PasswordView
====
下划线（方框）密码输入框
##Demo
<img src="https://raw.githubusercontent.com/EoniJJ/PasswordView/master/Demo_underline.png" width = "360" height = "640" />
<img src="https://raw.githubusercontent.com/EoniJJ/PasswordView/master/Demo_rect.png" width = "360" height = "640" />
## How to use ? 
 ` in layout.xml `
 ```xml
  <com.arron.passwordview.PasswordView
        android:id="@+id/passwordView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        app:mode="rect"
        app:passwordSize="40dp"/>
```
 详细请下载[Demo](https://codeload.github.com/EoniJJ/PasswordView/zip/master)查看
## Attribute
+ passwordLength (密码长度)
+ mode (样式切换 underline->下划线  rect->边框)
+ passwordPadding (每个密码框间隔)
+ passwordSize (单个密码框大小)
+ borderColor （边框颜色）
+ borderWidth（边框大小）
+ cursorFlashTime（光标闪烁间隔时间）
+ isCursorEnable（是否启用光标）
+ cipherTextSize（‘*’号大小）
+ cursorColor（光标颜色）

License
-------
    
        Copyright 2016 EoniJJ
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
