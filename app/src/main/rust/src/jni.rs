use std::ffi::CStr;
use std::panic;
use std::sync::Mutex;

use jni::objects::{JClass, JObject, JString, JValue, JValueGen};
use jni::sys::{jboolean, jint, jlong, jobject, jobjectArray, jsize, jstring};
use jni::{JNIEnv, JavaVM};
use librime_sys::{rime_get_api, RimeKeyCode, RimeModifier};
use once_cell::sync::Lazy;
use rime_api::{
    create_session, finalize, full_deploy_and_wait, initialize, set_notification_handler, setup,
    start_maintenance, Context, DeployResult, KeyEvent, KeyStatus, Session, Traits,
};

use crate::helper::{jni_log, null_jobject, CheckOrThrow};
use crate::{
    declare_librime_module_dependencies, APP_NAME, DISTRIBUTION_CODE_NAME, DISTRIBUTION_NAME,
    DISTRIBUTION_VERSION,
};

static JAVA_VM: Lazy<Mutex<Option<JavaVM>>> = Lazy::new(|| Mutex::new(None));

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_jniInit(env: JNIEnv, _: JClass) {
    declare_librime_module_dependencies();
    let jvm = env.get_java_vm().unwrap();
    JAVA_VM.lock().unwrap().replace(jvm);
    panic::set_hook(Box::new(|x| {
        let info = format!("{}", x);
        let guard = JAVA_VM.lock().unwrap();
        let jvm = guard.as_ref().unwrap();
        let mut env = jvm.attach_current_thread().unwrap();
        let err_text = format!("Rust panic!!\n{}", info);
        jni_log(&mut env, &err_text).unwrap();
        let _ = env.throw(err_text);
    }));
}

static RIME_SETUP_FLAG: Lazy<Mutex<bool>> = Lazy::new(|| Mutex::new(false));

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_pers_zhc_android_rime_rime_JNI_initialize(
    mut env: JNIEnv,
    _: JClass,
    user_data_dir: JString,
    shared_data_dir: JString,
) {
    jni_log(&mut env, "Rime initialize").unwrap();

    let mut traits = Traits::new();
    traits.set_distribution_name(DISTRIBUTION_NAME);
    traits.set_distribution_code_name(DISTRIBUTION_CODE_NAME);
    traits.set_distribution_version(DISTRIBUTION_VERSION);
    traits.set_app_name(APP_NAME);
    let result: anyhow::Result<()> = try {
        // TODO: non-UTF8 path
        traits.set_user_data_dir(env.get_string(&user_data_dir)?.to_str()?);
        if !shared_data_dir.is_null() {
            traits.set_shared_data_dir(env.get_string(&shared_data_dir)?.to_str()?);
        }
    };
    result.check_or_throw(&mut env).unwrap();
    if !*RIME_SETUP_FLAG.lock().unwrap() {
        setup(&mut traits);
        *RIME_SETUP_FLAG.lock().unwrap() = true;
    }
    initialize(&mut traits);
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_pers_zhc_android_rime_rime_JNI_finalize(
    mut env: JNIEnv,
    _class: JClass,
) {
    jni_log(&mut env, "Rime finalize").unwrap();
    finalize();
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_pers_zhc_android_rime_rime_JNI_getRimeVersion(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let version = unsafe {
        let api = rime_get_api();
        CStr::from_ptr((*api).get_version.unwrap()())
            .to_string_lossy()
            .to_string()
    };
    env.new_string(version).unwrap().into_raw()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_deploy(
    mut env: JNIEnv,
    _class: JClass,
) {
    jni_log(&mut env, "Rime deploy").unwrap();
    start_maintenance(false).check_or_throw(&mut env).unwrap();
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_fullDeployAndWait(
    mut env: JNIEnv,
    _class: JClass,
) -> jboolean {
    jni_log(&mut env, "Rime full deploy").unwrap();
    let deploy_result = full_deploy_and_wait();
    match deploy_result {
        DeployResult::Success => true,
        DeployResult::Failure => false,
    }
    .into()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_createSession(
    mut env: JNIEnv,
    _class: JClass,
) -> jlong {
    let session = create_session();
    if session.is_err() {
        env.throw("Session creation failed").unwrap();
    }
    Box::into_raw(Box::new(session.unwrap())) as *mut Session as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_processKey(
    _env: JNIEnv,
    _class: JClass,
    session: jlong,
    key_code: jint,
    modifier: jint,
) -> jboolean {
    let session = &*(session as *const Session);
    let status = session.process_key(KeyEvent::new(
        key_code as RimeKeyCode,
        modifier as RimeModifier,
    ));
    match status {
        KeyStatus::Accept => true,
        KeyStatus::Pass => false,
    }
    .into()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_closeSession(
    mut env: JNIEnv,
    _class: JClass,
    session: jlong,
) {
    let mut session = Box::from_raw(session as *mut Session);
    if session.close().is_err() {
        env.throw("Failed to close session").unwrap();
    }
    drop(session);
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_getContext(
    _env: JNIEnv,
    _class: JClass,
    session: jlong,
) -> jlong {
    let session = &*(session as *const Session);
    match session.context() {
        None => 0,
        Some(c) => Box::into_raw(Box::new(c)) as jlong,
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_getPreedit(
    mut env: JNIEnv,
    _class: JClass,
    context: jlong,
) -> jstring {
    let context = &*(context as *const Context);
    let Some(preedit) = context.composition().preedit else {
        return null_jobject()
    };
    let preedit = env.new_string(preedit);
    preedit.check_or_throw(&mut env).unwrap();
    if preedit.is_err() {
        return null_jobject();
    }
    preedit.unwrap().into_raw()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_getCandidates(
    mut env: JNIEnv,
    _class: JClass,
    context: jlong,
    dummy_candidate_obj: JObject,
) -> jobjectArray {
    let result: anyhow::Result<jobjectArray> = try {
        let context = &*(context as *const Context);
        let candidate_class = env.get_object_class(dummy_candidate_obj)?;
        let candidates = &context.menu().candidates;

        let candidates_array = env.new_object_array(
            candidates.len() as jsize,
            &candidate_class,
            &JObject::null(),
        )?;
        let select_labels = &context.select_labels();

        for (i, c) in candidates.iter().enumerate() {
            let comment_jstring: JString = match c.comment {
                None => JObject::null().into(),
                Some(c) => env.new_string(c)?,
            };
            let select_labels_jstring = match select_labels {
                None => JObject::null().into(),
                Some(s) => env.new_string(s[i])?,
            };

            let candidate_obj = env.new_object(
                &candidate_class,
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                &[
                    JValue::Object(&select_labels_jstring),
                    JValue::Object(&env.new_string(c.text)?.into()),
                    JValueGen::Object(&comment_jstring.into()),
                ],
            )?;
            env.set_object_array_element(&candidates_array, i as jsize, candidate_obj)?;
        }
        candidates_array.into_raw()
    };
    result.check_or_throw(&mut env).unwrap();
    if result.is_err() {
        return null_jobject();
    }
    result.unwrap()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_getSelectedCandidatesPos(
    _env: JNIEnv,
    _class: JClass,
    context: jlong,
) -> jint {
    let context = &*(context as *const Context);
    context.menu().highlighted_candidate_index as jint
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_getCommit(
    mut env: JNIEnv,
    _class: JClass,
    session: jlong,
) -> jstring {
    let session = &*(session as *const Session);
    let result: anyhow::Result<jstring> = match session.commit() {
        None => return null_jobject(),
        Some(c) => try { env.new_string(c.text())?.into_raw() },
    };
    result.check_or_throw(&mut env).unwrap();
    if result.is_err() {
        return null_jobject();
    }
    result.unwrap()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_releaseContext(
    _env: JNIEnv,
    _class: JClass,
    context: jlong,
) {
    let context = context as *mut Context;
    drop(Box::from_raw(context));
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_setNotificationHandler(
    mut env: JNIEnv,
    _class: JClass,
    callback: JObject,
) {
    if callback.is_null() {
        set_notification_handler(|_, _| {});
        return;
    }

    let result: anyhow::Result<()> = try {
        let global_callback = env.new_global_ref(callback)?;
        let java_vm = env.get_java_vm()?;

        set_notification_handler(move |t, v| {
            let result: anyhow::Result<()> = try {
                let mut guard = java_vm.attach_current_thread()?;
                let env = &mut *guard;
                let r#type = env.new_string(t)?;
                let value = env.new_string(v)?;
                env.call_method(
                    &*global_callback,
                    "onMessage",
                    "(Ljava/lang/String;Ljava/lang/String;)V",
                    &[
                        JValue::Object(&r#type.into()),
                        JValue::Object(&value.into()),
                    ],
                )?;
            };
            // tested. no error should occur
            assert!(result.is_ok());
        });
    };
    result.check_or_throw(&mut env).unwrap();
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_getStatus(
    mut env: JNIEnv,
    _class: JClass,
    session: jlong,
    dummy_status: JObject,
) -> jobject {
    let result: anyhow::Result<jobject> = try {
        let session = &*(session as *const Session);
        let status = session.status()?;
        let schema_name = env.new_string(status.schema_name())?;
        let schema_id = env.new_string(status.schema_id())?;

        let status_class = env.get_object_class(&dummy_status)?;
        env.new_object(
            &status_class,
            "(Ljava/lang/String;Ljava/lang/String;ZZZZZZZ)V",
            &[
                JValue::Object(&schema_name),
                JValue::Object(&schema_id),
                JValue::Bool(status.is_disabled.into()),
                JValue::Bool(status.is_composing.into()),
                JValue::Bool(status.is_ascii_mode.into()),
                JValue::Bool(status.is_full_shape.into()),
                JValue::Bool(status.is_simplified.into()),
                JValue::Bool(status.is_traditional.into()),
                JValue::Bool(status.is_ascii_punct.into()),
            ],
        )?
        .into_raw()
    };
    if result.is_err() {
        result.check_or_throw(&mut env).unwrap();
    }
    result.unwrap()
}
