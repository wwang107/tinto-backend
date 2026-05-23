package tinto.auth

import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/auth")
class AuthResource {

    @Inject
    lateinit var authService: AuthService

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun login(request: LoginRequest): AuthResponse {
        if (request.provider.isBlank() || request.idToken.isBlank()) {
            throw BadRequestException("provider and idToken are required")
        }
        return authService.login(request)
    }
}
