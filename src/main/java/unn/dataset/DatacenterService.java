package unn.dataset;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.HashMap;
import java.util.List;

public interface DatacenterService {
    @GET("/agent/miner/dataset/body")
    Call<String> fetchDataset(@Body HashMap<String, List<String>> options);

    @GET("/dataset/features/random/layer/{layer}")
    Call<HashMap<String, List<String>>> getRandomFeatures(@Path("layer") int layer);

    @GET("/dataset/register")
    Call<String> registerAgent(@Body DatasetDescriptor descriptor);

}
