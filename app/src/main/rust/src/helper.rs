use std::fmt::Debug;

use jni::objects::JObject;
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
