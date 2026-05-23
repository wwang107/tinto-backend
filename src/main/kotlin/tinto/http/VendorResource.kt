package tinto.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.security.Authenticated
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.delay
import java.io.InputStream
import java.nio.charset.StandardCharsets

@Authenticated
@Path("/vendors")
class VendorResource {

    @Inject
    lateinit var objectMapper: ObjectMapper

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun searchVendors(
        @QueryParam("q") type: String?,
        @QueryParam("name") name: String?
    ): List<JsonNode> {
        val resource = when (type?.lowercase()) {
            "cafes" -> "/cafes.json"
            else -> return emptyList()
        }
        val inputStream: InputStream = VendorResource::class.java.getResourceAsStream(resource)
            ?: return emptyList()
        val vendors: List<JsonNode> = objectMapper.readValue(
            inputStream,
            objectMapper.typeFactory.constructCollectionType(List::class.java, JsonNode::class.java)
        )
        if (name.isNullOrBlank()) return vendors
        val q = name.lowercase()
        return vendors.filter { vendor ->
            vendor["name"]?.asText("").orEmpty().lowercase().contains(q)
        }
    }

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