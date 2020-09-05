package unn.structures;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import unn.dataset.DatacenterService;
import unn.dataset.MaestroService;

public class Utils {

    public static DatacenterService getDatacenter(boolean isJson) {
        Retrofit.Builder retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s://%s:%d",
                    Config.DATACENTER_PROTOCOL,
                    Config.DATACENTER_HOST,
                    Config.DATACENTER_PORT));
        retrofit.addConverterFactory(ScalarsConverterFactory.create());
        retrofit.addConverterFactory(JacksonConverterFactory.create());
        DatacenterService service = retrofit.build().create(DatacenterService.class);
        return service;
    }

    public static MaestroService getMaestro() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("%s://%s:%d",
                        Config.MAESTRO_PROTOCOL,
                        Config.MAESTRO_HOST,
                        Config.MAESTRO_PORT))
                .addConverterFactory(JacksonConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MaestroService service = retrofit.create(MaestroService.class);
        return service;
    }
}
