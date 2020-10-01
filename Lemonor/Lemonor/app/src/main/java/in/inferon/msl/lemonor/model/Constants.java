package in.inferon.msl.lemonor.model;


import android.Manifest;
import android.content.Context;
import com.facebook.appevents.AppEventsLogger;
import in.inferon.msl.lemonor.repo.Repository;


public class Constants {
    public static final String BOLD_FONT = "CircularAir-Bold.otf";
    public static final String LIGHT_FONT = "CircularAir-Light.otf";

    public static final String BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/php/version/index.php/";
//            public static final String BASE_URL = "http://glancer.in/lemonor/cab/php/v128/index.php/";
    public static final String IMG_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/assets/offers/";
//            public static final String IMG_BASE_URL = "http://glancer.in/lemonor/cab/assets/offers/";
    public static final String SM_IMG_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/assets/sm/";
//            public static final String SM_IMG_BASE_URL = "http://glancer.in/lemonor/cab/assets/sm/";
    public static final String TEXT_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/";
//            public static final String TEXT_BASE_URL = "http://glancer.in/lemonor/cab/";
    public static final String SHOP_IMG_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/images/shop/";
//        public static final String SHOP_IMG_BASE_URL = "http://glancer.in/lemonor/cab/images/shop/";
    public static final String CATEGORY_IMG_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/images/category/";
//        public static final String CATEGORY_IMG_BASE_URL = "http://glancer.in/lemonor/cab/images/category/";
    public static final String SUPPLIER_PRODUCTS_IMG_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/images/supplier_products/";
//        public static final String SUPPLIER_PRODUCTS_IMG_BASE_URL = "http://glancer.in/lemonor/cab/images/supplier_products/";
    public static final String PRODUCTS_IMG_BASE_URL = "http://glancer.in/prototypes/flico/d117/projects_main/lemonor/images/products/";
//        public static final String PRODUCTS_IMG_BASE_URL = "http://glancer.in/lemonor/cab/images/products/";


    public static AppEventsLogger logger;
    public static Context context;
    public static String user_id;
    public static String country;
    public static Repository repository;
    public final static int PAGE_SIZE = 30;
    public static double changedLatitude = 0.0;
    public static double changedLongitude = 0.0;

    public static String[] getPermissions() {
        return new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.GET_ACCOUNTS
        };
    }

    public static String[] getLocationPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }
}

