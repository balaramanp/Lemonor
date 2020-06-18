package in.inferon.msl.cucumbor.model;


import android.Manifest;


public class Constants {
    public static final String BOLD_FONT = "CircularAir-Bold.otf";
    public static final String LIGHT_FONT = "CircularAir-Light.otf";


//    public static final String BASE_URL = "http://65e14641.ngrok.io/praveen/live/tfm/the_first_market_v2-modularising/php/";
//    public static final String IMAGE_BASE_URL = "http://65e14641.ngrok.io/praveen/live/tfm/the_first_market_v2-modularising/assets/product_images/";
        public static final String BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/tfm_tmp_vectra/php/";
        public static final String IMAGE_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/tfm_tmp_vectra/assets/product_images/";


    public static String[] getPermissions() {
        return new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CALL_PHONE
        };
    }
}

