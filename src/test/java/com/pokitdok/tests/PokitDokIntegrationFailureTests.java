package com.pokitdok.tests;

import com.pokitdok.UnauthorizedException;
import com.pokitdok.PokitDok;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.fail;

public class PokitDokIntegrationFailureTests {
    private static PokitDok pd;
    private static Map<String, String> env = System.getenv();

    @BeforeClass
    public static void setup() throws Exception {

        String clientId = "INVALID_CLIENT_ID";
        String clientSecret = "INVALID_CLIENT_SECRET";
        if ((clientId == null) || (clientSecret == null)) {
            fail("Please provide a PokitDok client ID and secret in the environment variables PD_CLIENT_ID and PD_CLIENT_SECRET.");
        }

        String apiBase = env.get("PD_API_BASE");
        pd = new PokitDok(clientId, clientSecret, null, apiBase);
    }

    @Test(expected = UnauthorizedException.class)
    public void unauthorizedExceptionTest() throws Exception {
        pd.activities();
    }

}
