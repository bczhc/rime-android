mod jni;

#[link(name = "rime")]
extern "C" {
    pub fn declare_librime_module_dependencies();
}

const APP_NAME: &str = "Rime-Android";
const DISTRUBUTION_NAME: &str = "Rime-Android";
const DISTRUBUTION_CODE_NAME: &str = "Rime-Android";
const DISTRUBUTION_VERSION: &str = "0.0.0";
