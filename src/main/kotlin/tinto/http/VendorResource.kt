package tinto.http

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import java.io.InputStream
import java.nio.charset.StandardCharsets

@Path("/vendors")
class VendorResource {

    @GET
    @Path("cafes")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCafes(): String {
        val inputStream: InputStream? = VendorResource::class.java.getResourceAsStream("/cafes.json")
        return inputStream?.bufferedReader(StandardCharsets.UTF_8)?.readText() ?: "[]"
    }

    @POST
    @Path("cafes")
    @Produces(MediaType.APPLICATION_JSON)
    fun createCafes() {

    }
}