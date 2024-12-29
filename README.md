# application-mock-tool

## 概要説明

- nodeやsass環境を作って画面開発をするまでもないようなモック画面の開発をする際の、簡易ビルドツール

## できること

- 以下のコメント文字を検知して、作成したコンポーネントファイルの中身に置き換えをすることができます
  - `<!-- include:コンポーネントファイル名-->`

- 例）hoge.htmlの中に定義しているヘッダーコメントを、header.htmlの中身と置換

```hoge.html

<div>
  <!-- include:header.html -->
</div>

```

```header.html

<div>
  <div>ヘッダーファイル</div>
</div>

```



## 使い方

- env.batを開いてJava実行環境を設定

```bat
@echo off

rem JAVA_HOME設定
set PATH=C:\java\openjdk-11.0.2_windows-x64_bin\jdk-11.0.2\bin;%PATH%

exit /b 0

```

- watch.batを開いて、以下の引数箇所を編集

```watch.bat
@echo off

set "CURRENT_DIR=%~dp0"

call env.bat

rem 第一引数：srcフォルダ（このフォルダ配下で変更されたファイルがあれば、distフォルダに反映）
rem 第二引数：distフォルダ（コンポーネントファイル置換後（ビルド後）の成果物を格納）
rem 第三引数：srcフォルダ配下で更新された対象ファイルの拡張子を指定　※HTMLファイルを指定する場合：.*.html ※全ファイルの場合：.*
java -cp %CURRENT_DIR%build-tool FileMonitor C:\\git\\node\\application-mock-tool\\src C:\\git\\node\\application-mock-tool\\dist .*.html

pause

exit 0

```



