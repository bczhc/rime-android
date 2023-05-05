use std::fmt::Debug;

use jni::objects::JObject;
use jni::objects::JValue;
use jni::sys::jobject;
use jni::JNIEnv;

pub trait CheckOrThrow {
    fn check_or_throw(&self, env: &mut JNIEnv) -> jni::errors::Result<()>;
}

impl<T, E> CheckOrThrow for Result<T, E>
where
    E: Debug,
{
    fn check_or_throw(&self, env: &mut JNIEnv) -> jni::errors::Result<()> {
        if let Err(e) = self {
            env.throw(format!("Error: {:?}", e))?;
        }
        Ok(())
    }
}

pub fn null_jobject() -> jobject {
    JObject::null().into_raw()
}

const JNI_LOG_TAG: &str = "jni-log";

pub fn log(env: &mut JNIEnv, tag: &str, msg: &str) -> jni::errors::Result<()> {
    let tag = env.new_string(tag)?;
    let msg = env.new_string(msg)?;

    let class = env.find_class("android/util/Log")?;
    env.call_static_method(
        class,
        "d",
        "(Ljava/lang/String;Ljava/lang/String;)I",
        &[JValue::Object(&tag.into()), JValue::Object(&msg.into())],
    )?;
    Ok(())
}

pub fn jni_log(env: &mut JNIEnv, msg: &str) -> jni::errors::Result<()> {
    log(env, JNI_LOG_TAG, msg)
}
