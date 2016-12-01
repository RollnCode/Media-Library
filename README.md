# Media Library

Simple library for Camera like in WhatsApp. 

## Getting Started

### Dependency

Include the dependency [Download (.aar)](https://github.com/RollnCode/Media-Library/blob/master/release/library-release.aar) and place it in your libs directory:

```groovy
allprojects {
    repositories {
        jcenter()
        flatDir {
            dirs 'libs'
        }
    }
}

// ...

dependencies {
    compile (name:'media', ext:'aar')
    compile 'com.android.support:appcompat-v7:24.2.1'
    compile 'com.android.support:recyclerview-v7:24.2.1' // you need include this too
}
```

### Usage

You need simply start CameraActivity from startActivityForResult. You can use CameraActivity.start();

```java
CameraActivity.start(MainActivity.this, REQUEST_CAMERA, CamcorderProfile.QUALITY_720P);
```

Result of CameraActivity work is String path to file:

```java
if (data.hasExtra(CameraActivity.EXTRA_FILE_PATH)) {
	final String path = data.getStringExtra(CameraActivity.EXTRA_FILE_PATH);
}
```