#![feature(try_blocks)]

mod helper;
mod jni;

#[link(name = "rime")]
extern "C" {
    pub fn declare_librime_module_dependencies();
}

const APP_NAME: &str = "Rime-Android";
const DISTRIBUTION_NAME: &str = "Rime-Android";
const DISTRIBUTION_CODE_NAME: &str = "Rime-Android";
const DISTRIBUTION_VERSION: &str = "0.0.0";
