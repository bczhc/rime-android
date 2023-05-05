use std::ffi::CStr;
use std::time::Duration;

use jni::objects::{JClass, JObject, JString, JValue, JValueGen};
use jni::sys::{jboolean, jint, jlong, jobjectArray, jsize, jstring};
use jni::JNIEnv;
use librime_sys::{rime_get_api, RimeKeyCode, RimeModifier};
use rime_api::engine::{DeployResult, Engine};
use rime_api::{Context, KeyEvent, KeyStatus, Session, Traits};

use crate::helper::{null_jobject, CheckOrThrow};
use crate::{
    declare_librime_module_dependencies, APP_NAME, DISTRIBUTION_CODE_NAME, DISTRIBUTION_NAME,
    DISTRIBUTION_VERSION,
};

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_pers_zhc_android_rime_rime_JNI_initModules(_env: JNIEnv, _: JClass) {
    unsafe {
        declare_librime_module_dependencies();
    }
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
pub extern "system" fn Java_pers_zhc_android_rime_rime_JNI_createEngine(
    mut env: JNIEnv,
    _class: JClass,
    userDataDir: JString,
    sharedDataDir: JString,
) -> jlong {
    let result: anyhow::Result<jlong> = try {
        // TODO: non-UTF8 path
        let mut traits = Traits::new();
        traits.set_user_data_dir(env.get_string(&userDataDir)?.to_str()?);
        if !sharedDataDir.is_null() {
            traits.set_shared_data_dir(env.get_string(&sharedDataDir)?.to_str()?);
        }
        traits.set_distribution_name(DISTRIBUTION_NAME);
        traits.set_distribution_code_name(DISTRIBUTION_CODE_NAME);
        traits.set_distribution_version(DISTRIBUTION_VERSION);
        traits.set_app_name(APP_NAME);

        Box::into_raw(Box::new(Engine::new(traits))) as jlong
    };
    if result.is_err() {
        return 0;
    }
    result.unwrap()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_waitForDeployment(
    _env: JNIEnv,
    _class: JClass,
    engine: jlong,
) -> jboolean {
    let engine = &mut *(engine as *mut Engine);
    let deploy_result = engine.wait_for_deploy_result(Duration::from_secs_f32(0.1));
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
    engine: jlong,
) -> jlong {
    let engine = &mut *(engine as *mut Engine);
    if engine.create_session().is_err() {
        env.throw("Session creation failed").unwrap();
    }
    engine.session().unwrap() as *const Session as jlong
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
    let session = Box::from_raw(session as *mut Session);
    if session.close().is_err() {
        env.throw("Failed to close session").unwrap();
    }
    drop(session)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_rime_JNI_releaseEngine(
    _env: JNIEnv,
    _class: JClass,
    engine: jlong,
) {
    drop(Box::from_raw(engine as *mut Engine));
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
    let Some(preedit) = context.composition.preedit else {
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
        let candidates = &context.menu.candidates;

        let candidates_array = env.new_object_array(
            candidates.len() as jsize,
            &candidate_class,
            &JObject::null(),
        )?;
        let select_labels = &context.select_labels;

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
    context.menu.highlighted_candidate_index as jint
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
        Some(c) => try { env.new_string(c.text)?.into_raw() },
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
    engine: jlong,
    callback: JObject,
) {
    let engine = &mut *(engine as *mut Engine);

    if callback.is_null() {
        engine.set_notification_callback(|_, _| {});
        return;
    }

    let result: anyhow::Result<()> = try {
        // TODO: memory leak
        let global_callback = env.new_global_ref(callback)?;
        let java_vm = env.get_java_vm()?;

        engine.set_notification_callback(move |t, v| {
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
            // tested. shouldn't panic
            result.unwrap();
        });
    };
    result.check_or_throw(&mut env).unwrap();
}
