package unn.dataset;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.HashMap;
import java.util.List;

public interface DatacenterService {
    @Headers({"Accept:text/plain"})
    @POST("/agent/miner/dataset/body")
    Call<String> fetchDataset(@Body HashMap<String, List<String>> options);

    @GET("/dataset/features/random/layer/{layer}")
    Call<HashMap<String, List<String>>> getRandomFeatures(@Path("layer") int layer);

    @GET("/dataset/register")
    Call<String> registerAgent(@Body DatasetDescriptor descriptor);

}
