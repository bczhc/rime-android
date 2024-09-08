RimeAndroid
---

On Android mobile phone:<br/>
<img style="width: 50%; height: 50%;" src="https://github.com/bczhc/rime-android/assets/49330580/9812c8ae-b23b-4d28-88fa-f16eab83796d"/>

On Android tablet: (horizontal candidates)<br/>
<img style="width: 100%;" src="https://github.com/user-attachments/assets/9699631d-50ff-4dc9-9aba-470b1e471241"/>


## Build

TODO (sorry for my laziness).

See [Dockerfile](https://github.com/bczhc/rime-android/blob/master/Dockerfile).

For the build of librime, please refer to this [gist](https://gist.github.com/bczhc/cd271734f812f32b4c9c70c766f1c95f).

## Known Issues
- May crash at specific scenarios. (more like a lack of error handling etc. Not that serious, or I'll fix it...)
- Candidates won't show on Android14. (I tested Xiaomi 14 Pro). 🤬

Note this app is developed only for my personal use. I've tried Trime or other Android distributions for Rime (like "中文输入法"),
however they all behave differently as I expected, plus, randomly crash etc. Yea I may misconfigured the config files
and missed some specific configs for these Android Rime clients, but
for me, I only input via physical keyboards on Android,
and I hope it uses exactly the same config as what my fcitx5-rime uses - thus my idea is to keep
it as simple as possible.

BTW in the early days,
I developed an independent [IME](https://github.com/bczhc/some-tools/tree/351efade50f2a3dc9c2b9d1613558384730ab946/app/src/main/java/pers/zhc/tools/wubi) on Android.
I should've discovered librime earlier :)
