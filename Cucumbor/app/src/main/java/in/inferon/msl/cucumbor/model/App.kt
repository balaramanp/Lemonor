package `in`.inferon.msl.cucumbor.model

import android.app.Application
import android.content.Context
import android.util.Log
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.annotation.AcraHttpSender
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.sender.HttpSender

@AcraHttpSender(uri = "http://glancer.in/prototypes/flico/d117/projects_main/tfm_tmp_vectra/acra/index.php", httpMethod = HttpSender.Method.POST)

class App : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Log.e("attachBaseContext", "Incomming")
        // The following line triggers the initialization of ACRA
        val builder = CoreConfigurationBuilder(this)
        builder.setBuildConfigClass(BuildConfig::class.java).setReportFormat(StringFormat.JSON)
        //builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class).setResText(R.string.acra_toast_text);
        ACRA.init(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}