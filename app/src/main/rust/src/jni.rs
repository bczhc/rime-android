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
    declare_librime_module_dependencies, APP_NAME, DISTRUBUTION_CODE_NAME, DISTRUBUTION_NAME,
    DISTRUBUTION_VERSION,
};

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_pers_zhc_android_rime_jni_Rime_initModules(_env: JNIEnv, _: JClass) {
    unsafe {
        declare_librime_module_dependencies();
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_pers_zhc_android_rime_jni_Rime_getRimeVersion(
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
pub extern "system" fn Java_pers_zhc_android_rime_jni_Rime_createEngine(
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
        traits.set_distribution_name(DISTRUBUTION_NAME);
        traits.set_distribution_code_name(DISTRUBUTION_CODE_NAME);
        traits.set_distribution_version(DISTRUBUTION_VERSION);
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
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_waitForDeployment(
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
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_createSession(
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
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_processKey(
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
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_closeSession(
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
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_closeEngine(
    _env: JNIEnv,
    _class: JClass,
    engine: jlong,
) {
    drop(Box::from_raw(engine as *mut Engine));
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_getContext(
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
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_getPreedit(
    mut env: JNIEnv,
    _class: JClass,
    context: jlong,
) -> jstring {
    let context = &*(context as *const Context);
    let Some(preedit) = context.composition.preedit else {
        return JObject::null().into_raw()
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
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_getCandidates(
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

        for (i, c) in candidates.iter().enumerate() {
            let comment_jstring: JString = match c.comment {
                None => JObject::null().into(),
                Some(c) => env.new_string(c)?,
            };

            let candidate_obj = env.new_object(
                &candidate_class,
                "(Ljava/lang/String;Ljava/lang/String;)V",
                &[
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
        return JObject::null().into_raw();
    }
    result.unwrap()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_getSelectedCandidatesPos(
    _env: JNIEnv,
    _class: JClass,
    context: jlong,
) -> jint {
    let context = &*(context as *const Context);
    context.menu.highlighted_candidate_index as jint
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_pers_zhc_android_rime_jni_Rime_getCommit(
    env: JNIEnv,
    _class: JClass,
    session: jlong,
) -> jstring {
    let session = &*(session as *const Session);
    match session.commit() {
        None => return JObject::null().into_raw(),
        Some(c) => env.new_string(c.text).unwrap().into_raw(),
    }
}
