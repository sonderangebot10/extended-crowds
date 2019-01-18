<?php
    define('ROOT_PATH', dirname(__DIR__) . '/');
    define('PHP_URL', 'http://217.211.176.94:1212');
    define('FIREBASE_API_KEY', 'AAAAi_AEfJM:APA91bHfYvAFqbc_U42hx-NADaR3iwTfVdPwkicTDDEdDYWHOd0eU-ssxJuRfWaBXEdrSiIoPbz13l_wFNqcr3JWeSrS7hsxn7hK7QzVVPjlL6j93qQP-ZHjOxNLSpZORsAkqd3hwEqj');
    define('DB_PATH', ROOT_PATH . 'html/database/');
    define('QC_PATH', ROOT_PATH . 'html/quality_control/');
    define('REP_PATH', ROOT_PATH . 'html/reputation/');
    define('TA_PATH', ROOT_PATH . 'html/task_allocation/');
    define('REW_PATH', ROOT_PATH . 'html/reward/');

    define('MAX_DEVICES', 1);

    // set log 1 to activate
    define('LOG', 1);

    // set maintance to 1 to activate
    define('MAINTENANCE', 0);
    define('MAINTENANCE_TIME', '13:00');

    define('HB_TIME', '1 hours'); //hours
    define('HB_CHECK_TIME', '10 minutes'); //minutes
    
    $dynamic_settings = json_decode(file_get_contents(ROOT_PATH."html/settings.json"), true);
    define('DB', DB_PATH . $dynamic_settings['DB']);
    define('REP', REP_PATH . $dynamic_settings['REP']);
    define('TA', TA_PATH . $dynamic_settings['TA']);
    define('REW', REW_PATH . $dynamic_settings['REW']);
    define('ANDROID_VERSION', $dynamic_settings['ANDROID_VERSION']);
    define('IOS_VERSION', $dynamic_settings['IOS_VERSION']);
    define('QC_SINGLE', QC_PATH . $dynamic_settings['QC_SINGLE']);
    define('QC_MULTIPLE', QC_PATH . $dynamic_settings['QC_MULTIPLE']);
    define('QC_NUMERIC', QC_PATH . $dynamic_settings['QC_NUMERIC']);
    define('QC_LIGHT', QC_PATH . $dynamic_settings['QC_LIGHT']);
    define('QC_PRESSURE', QC_PATH . $dynamic_settings['QC_PRESSURE']);
    define('QC_ACCELEROMETER', QC_PATH . $dynamic_settings['QC_ACCELEROMETER']);
    define('QC_AMBIENT_TEMPERATURE', QC_PATH . $dynamic_settings['QC_AMBIENT_TEMPERATURE']);
?>