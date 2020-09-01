package unn.structures;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import unn.dataset.DatacenterService;
import unn.dataset.MaestroService;

public class Utils {

    public static DatacenterService getDatacenter() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("%s://%s:%d",
                        Config.DATACENTER_PROTOCOL,
                        Config.DATACENTER_HOST,
                        Config.DATACENTER_PORT))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        DatacenterService service = retrofit.create(DatacenterService.class);
        return service;
    }

    public static MaestroService getMaestro() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("%s://%s:%d",
                        Config.DATACENTER_PROTOCOL,
                        Config.DATACENTER_HOST,
                        Config.DATACENTER_PORT))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MaestroService service = retrofit.create(MaestroService.class);
        return service;
    }
}
