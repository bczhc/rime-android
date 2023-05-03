use std::ffi::CStr;

use jni::objects::JClass;
use jni::sys::jstring;
use jni::JNIEnv;
use librime_sys::rime_get_api;

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
