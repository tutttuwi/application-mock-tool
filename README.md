# application-mock-tool

## �T�v����

- node��sass��������ĉ�ʊJ��������܂ł��Ȃ��悤�ȃ��b�N��ʂ̊J��������ۂ́A�ȈՃr���h�c�[��

## �ł��邱��

- �ȉ��̃R�����g���������m���āA�쐬�����R���|�[�l���g�t�@�C���̒��g�ɒu�����������邱�Ƃ��ł��܂�
  - `<!-- include:�R���|�[�l���g�t�@�C����-->`

- ��jhoge.html�̒��ɒ�`���Ă���w�b�_�[�R�����g���Aheader.html�̒��g�ƒu��

```hoge.html

<div>
  <!-- include:header.html -->
</div>

```

```header.html

<div>
  <div>�w�b�_�[�t�@�C��</div>
</div>

```



## �g����

- env.bat���J����Java���s����ݒ�

```bat
@echo off

rem JAVA_HOME�ݒ�
set PATH=C:\java\openjdk-11.0.2_windows-x64_bin\jdk-11.0.2\bin;%PATH%

exit /b 0

```

- watch.bat���J���āA�ȉ��̈����ӏ���ҏW

```watch.bat
@echo off

set "CURRENT_DIR=%~dp0"

call env.bat

rem �������Fsrc�t�H���_�i���̃t�H���_�z���ŕύX���ꂽ�t�@�C��������΁Adist�t�H���_�ɔ��f�j
rem �������Fdist�t�H���_�i�R���|�[�l���g�t�@�C���u����i�r���h��j�̐��ʕ����i�[�j
rem ��O�����Fsrc�t�H���_�z���ōX�V���ꂽ�Ώۃt�@�C���̊g���q���w��@��HTML�t�@�C�����w�肷��ꍇ�F.*.html ���S�t�@�C���̏ꍇ�F.*
java -cp %CURRENT_DIR%build-tool FileMonitor C:\\git\\node\\application-mock-tool\\src C:\\git\\node\\application-mock-tool\\dist .*.html

pause

exit 0

```



