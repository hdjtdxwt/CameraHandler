package com.epsit.camerahandler.utils;

public interface Constants {
    String VERIFY_MODEL_NAME = "verify.model";
    String IMAGE_DIR = "face_search";
    String FACESEARCH_DB_NAME = "facesearch.db";
    int DB_VERSION = 1;
    int MAX_COUNT = 3;
    int PHOTO_REQUEST_CAREMA = 1;// 打开相机拍照
    int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    int CROP_REQUEST_IMAGE = 3; // 裁剪
    String SEARCH_RESULTLIST_KEY = "search_result";
    String SRC_IMAGEPATH_KEY = "src_path";
    String GALLERY_DATA = "data";
    int THUMBNAIL_HEIGHT = 100;
    int THUMBNAIL_WIDTH = 100;
    String THUMBNAIL_DIR_NAME = "thumbnail";
    String TAKEPIC_DIR_NAME = "images";
    String PROVIDER_NAME = "com.sensetime.facesearchsample.provider";
}
