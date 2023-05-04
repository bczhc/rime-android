use crate::{
    declare_librime_module_dependencies, APP_NAME, DISTRUBUTION_CODE_NAME, DISTRUBUTION_NAME,
    DISTRUBUTION_VERSION,
};
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;
use librime_sys::{rime_get_api, RimeKeyCode, RimeModifier};
use rime_api::engine::{DeployResult, Engine};
use rime_api::{KeyEvent, KeyStatus, Session, Traits};
use std::ffi::CStr;
use std::time::Duration;

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
    // TODO: non-UTF8 path
    let mut traits = Traits::new();
    traits.set_user_data_dir(env.get_string(&userDataDir).unwrap().to_str().unwrap());
    if !sharedDataDir.is_null() {
        traits.set_shared_data_dir(env.get_string(&sharedDataDir).unwrap().to_str().unwrap());
    }
    traits.set_distribution_name(DISTRUBUTION_NAME);
    traits.set_distribution_code_name(DISTRUBUTION_CODE_NAME);
    traits.set_distribution_version(DISTRUBUTION_VERSION);
    traits.set_app_name(APP_NAME);

    Box::into_raw(Box::new(Engine::new(traits))) as jlong
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
