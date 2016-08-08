package com.pokitdok;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
    Main class of the pokitdok-java client access library for the PokitDok APIs.

    <p>

    The <a href="https://github.com/PokitDok/pokitdok-java">pokitdok-java</a> library provides a
    concise means for applications written in Java (and related languages) to integrate functionality
    from the <a href="https://pokitdok.com">PokitDok</a> health APIs. It handles user authentication,
    scoping for endpoints that require it, and parsing results.

    <p>

    <b>Basic usage example:</b>
    <pre>
    {@code
        import java.util.HashMap;
        import java.util.Map;
        import com.pokitdok.*;
        import org.json.simple.*;

        public class PokitDokTest {
            public static void main(String argv[]) {
                PokitDok pd = new PokitDok("client_id", "client_secret");

                Map<String, String> query = new HashMap<String, String>();
                query.put("last_name", "Aya-ay");

                Map<String, Object> results = pd.providers(query);
                System.out.println((JSONArray) results.get("data"));
            }
        }
    }
    </pre>

    For simplicity, pokitdok-java uses the json-simple library for parsing results.

    @version 0.9
    @author PokitDok, Inc.
*/
public class PokitDok {
    /** The current version of the library. */
    public static final String VERSION = "0.9";
    /** The API version the client uses. Currently this is v4, the only deployed version of
        the PokitDok APIs. */
    public static final String API_VERSION = "v4";
    /** Default URL used to access the PokitDok Platform. */
    public static final String DEFAULT_API_BASE = "https://platform.pokitdok.com";
    /** The default scope for most requests. */
    public static final String DEFAULT_SCOPE = "default";
    /** The default scope for scheduling requests. */
    public static final String USER_SCHEDULE_SCOPE = "user_schedule";

    /**
        A reference to a {@link PokitDokHTTPConnector} used by this instance. This Connector is used
        to isolate the client implementation from the HTTP library used (in the current implementation,
        the one from Apache.)
    */
    private PokitDokHTTPConnector connector;
    /** The PokitDok Client ID used by this instance. */
    private String clientId;
    /** The PokitDok Client Secret used by this instance. */
    private String clientSecret;
    /**
        The version of the PokitDok API being used by this instance. Note that the only
        valid value is 'v4', since that is the only deployed API version ('v3' was deprecated some
        time ago)
     */
    private String apiVersion;
    /**
        The base URL used to connect to the PokitDok Platform. Can be overridden for internal
        testing.
    */
    private static String apiBase = DEFAULT_API_BASE;
    /**
        A default set of HTTP headers transmitted with every request. Currently used to transmit
        client and JVM versions for logging purposes.
    */
    public static Map<String, String> defaultHeaders;
    /** An instance of JSONParser re-used for construction efficiency. */
    private JSONParser parser;

    /**
        Gets this client instance's API base, or the default if it is not set.

        @return this client instance's API base, or the default if it is not set.
    */
    public static String getApiBase() {
        return apiBase == null ? DEFAULT_API_BASE : apiBase;
    }

    /**
        Constructor which accepts an API base URL to override the default provided in
        {@link DEFAULT_API_BASE}.

        @param clientId a PokitDok client ID
        @param clientSecret a PokitDok client secret
        @param apiBase a new base URL to connect to for API calls
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
    */
    public PokitDok(String clientId, String clientSecret, String apiBase) throws IOException, ParseException {
        this(clientId, clientSecret, new ApacheHTTPConnector(clientId, clientSecret, getDefaultHeaders(), apiBase));
        this.apiBase = apiBase;
    }

    /**
        The ordinary constructor for client instances.

        @param clientId a PokitDok client ID
        @param clientSecret a PokitDok client secret
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
    */
    public PokitDok(String clientId, String clientSecret) throws IOException, ParseException {
        this(clientId, clientSecret, null, null);
    }

    /**
        Constructor which accepts a replacement {@link PokitDokHTTPConnector} to override the default.

        @param clientId a PokitDok client ID
        @param clientSecret a PokitDok client secret
        @param connector a new implementation of {@link PokitDokHTTPConnector} to override the default.
        @throws IOException usually implying a connectivity error reaching the platform server
    */
    public PokitDok(String clientId, String clientSecret, PokitDokHTTPConnector connector)
        throws IOException {
        this(clientId, clientSecret, connector, null);
    }

    /**
        Constructor which accepts a replacement {@link PokitDokHTTPConnector} to override the default,
        as well as a replacement API base URL for testing.

        @param clientId a PokitDok client ID
        @param clientSecret a PokitDok client secret
        @param connector a new implementation of {@link PokitDokHTTPConnector} to override the default.
        @param apiBase a String representing a platform URL to connect to
        @throws IOException usually implying a connectivity error reaching the platform server
    */
    public PokitDok(String clientId, String clientSecret, PokitDokHTTPConnector connector, String apiBase)
        throws IOException {
        this.clientId     = clientId;
        this.clientSecret = clientSecret;
	    this.apiBase      = apiBase != null ? apiBase : DEFAULT_API_BASE;
        this.connector    = connector != null ? connector : new ApacheHTTPConnector(clientId, clientSecret, getDefaultHeaders(), this.apiBase);
        this.parser       = new JSONParser();
    }

    /**
        Returns the default set of HTTP headers which are sent with every API request.

        @return a {@link Map} of header names and values
    */
    public static final Map<String, String> getDefaultHeaders() {
        if (defaultHeaders == null) {
            defaultHeaders = new HashMap<String, String>();
            defaultHeaders.put("User-Agent", "pokitdok-java " + PokitDok.VERSION +
                " jvm version " + System.getProperty("java.version"));
        }

        return defaultHeaders;
    }

    /**
        Constructs and returns a URL for the specified endpoint. If parameters are passed in,
        these are attached to the URL with a {@link URIBuilder}.

        @param apiBase the base Platform url to use to construct the url
        @param endpoint the PokitDok endpoint to invoke
        @param params the params to attach to the url (usually for GET requests)
        @return the URL for the given endpoint and the list of parameters
    */
    public static String apiUrl(String apiBase, String endpoint, Map<String, Object> params) {
      String uri = apiBase + "/api/" + API_VERSION + "/" + endpoint;

      if (null == params) {
          return uri;
      }

      if ((params != null) && (!params.isEmpty())) {
        try {
          URIBuilder uriWithParams = new URIBuilder(uri);

          for(String key: params.keySet()) {
            uriWithParams.addParameter(key, (String) params.get(key));
          }

          uri = uriWithParams.build().toString();
        }
        catch (URISyntaxException use) {
          uri = null;
        }
      }

      return uri;
    }

    /**
        Invokes the activities endpoint, with a HashMap of parameters.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> activities(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("activities", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the activities endpoint, with no parameters.

        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> activities() throws IOException, ParseException, UnauthorizedException {
        return activities(null);
    }

    /**
        Invokes the authorizations endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> authorizations(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.post("authorizations/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Queries the cash prices index.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> cashPrices(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("prices/cash", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Submits a claim.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> claims(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.post("claims/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the claims status endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> claimsStatus(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.post("claims/status", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Performs an eligibility check.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> eligibility(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.post("eligibility/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the enrollment endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> enrollment(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.post("enrollment", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Searches the insurance prices index.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> insurancePrices(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("prices/insurance", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the payers endpoint, with a HashMap of parameters.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> payers(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("payers", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the payers endpoint, with no parameters.

        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> payers() throws IOException, ParseException, UnauthorizedException {
        return payers(null);
    }

    /**
        Invokes the plans endpoint, with no parameters.

        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> plans() throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("plans", null, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the plans endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> plans(Map<String, Object> params) throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("plans", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the providers endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> providers(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("providers", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the providers endpoint, with no parameters.

        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> providers() throws IOException, ParseException, UnauthorizedException {
        return providers(null);
    }

    /**
        Invokes the referrals endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> referrals(Map<String, Object> params) throws IOException, ParseException, UnauthorizedException {
        String results = connector.post("referrals/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the trading partners endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> tradingPartners(Map<String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = "";
        if (params == null) {
            results = connector.get("tradingpartners/", params, defaultHeaders);
        }
        else if (params.containsKey("trading_partner_id")) {
            String tradingPartnerId = (String) params.remove("trading_partner_id");
            if (params.isEmpty()) {
                params = null;
            }
            results = connector.get("tradingpartners/" + tradingPartnerId, params, defaultHeaders);
        }

        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the trading partners endpoint, with no parameters.

        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> tradingPartners() throws IOException, ParseException, UnauthorizedException {
        return tradingPartners(null);
    }

    /* Scheduling endpoints */

    /**
        Queries for information about a single appointment.

        @param uuid the uuid of the appointment to search for
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> appointment(String uuid, Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("appointments/" + uuid, params, defaultHeaders, USER_SCHEDULE_SCOPE);
        return (JSONObject) parser.parse(results);
    }

    /**
        Queries for appointments.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> appointments(Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("appointments/", params, defaultHeaders, USER_SCHEDULE_SCOPE);
        return (JSONObject) parser.parse(results);
    }

    /**
        Queries for information about a single appointment type,

        @param type the specified appointment type
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> appointmentType(String type, Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("appointment_types/" + type, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Lists the available appointment types.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> appointmentTypes(Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("appointment_types/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Books a new appointment.

        @param uuid the uuid of the open appointment slot to book
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> bookAppointment(String uuid, Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.put("appointments/" + uuid, params, defaultHeaders, USER_SCHEDULE_SCOPE);
        return (JSONObject) parser.parse(results);
    }

    /**
        Cancels an appointment.

        @param uuid the uuid of the appointment to cancelAppointment
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> cancelAppointment(String uuid, Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.delete("appointments/" + uuid, params, defaultHeaders, USER_SCHEDULE_SCOPE);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the schedulers endpoint to return information about a specified scheduler.

        @param schedulerId the scheduler to search for
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> scheduler(String schedulerId, Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("schedulers/" + schedulerId, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the schedulers endpoint to list the available schedulers.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> schedulers(Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("schedulers/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the appointments endpoint to update an existing appointment,
        with a HashMap of parameters.

        @param uuid the uuid of the appointment to update
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> updateAppointment(String uuid, Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.put("appointments/" + uuid, params, defaultHeaders, USER_SCHEDULE_SCOPE);
        return (JSONObject) parser.parse(results);
    }

    /* Medical procedure code endpoints */

    /**
        Invokes the medical procedure code endpoint to search for MPCs.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> mpc(Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = connector.get("mpc/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the medical procedure code endpoint to search for a given MPC by code.

        @param code the MPC to search for
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> mpc(String code, Map <String, Object> params)
    throws IOException, ParseException, UnauthorizedException {
        String results = results = connector.get("mpc/" + code, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /* Identity Endpoints */

    /**
        Invokes the identity endpoint to create an identity resource.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> createIdentity(Map <String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
        String results = connector.post("identity/", params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the identity endpoint to update an identity resource.

        @param uuid the uuid of the identity to update
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> updateIdentity(String uuid, Map <String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
        String results = connector.put("identity/" + uuid, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the identity endpoint to search for an identity resource, with a UUID but no
        other parameters.

        @param uuid the UUID of the identity resource to search for
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> identity(String uuid)
            throws IOException, ParseException, UnauthorizedException {
        return identity(uuid, null);
    }

    /**
        Invokes the identity endpoint to search for an identity resource.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */

    public Map<String, Object> identity(Map<String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
        return identity(null, params);
    }

    /**
        Invokes the identity endpoint to search for an identity resource.

        @param uuid the UUID of the identity resource to search for
        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> identity(String uuid, Map<String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
        String urlString = "identity";
        if (null != uuid && !uuid.isEmpty()) {
            urlString += "/" + uuid;
        }
        String results = connector.get(urlString, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the pharmacy plans endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> pharmacyPlans(Map<String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
        String urlString = "pharmacy/plans";
        String results = connector.get(urlString, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the pharmacy formulary endpoint.

        @param params a Map of parameters to include with the request
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> pharmacyFormulary(Map<String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
        String urlString = "pharmacy/formulary";
        String results = connector.get(urlString, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the pharmacy network endpoint.

        @param params a Map of parameters to include with the request
        @param npi the NPI of a pharmacy to check in-network status
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> pharmacyNetwork(String npi, Map<String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
        String urlString = "pharmacy/network";
        if (null != npi && !npi.isEmpty()) {
            urlString += "/" + npi;
        }
        String results = connector.get(urlString, params, defaultHeaders);
        return (JSONObject) parser.parse(results);
    }

    /**
        Invokes the pharmacy network endpoint without an NPI

        @param params a Map of parameters to include with the request
        @param npi the NPI of a pharmacy to check in-network status
        @throws IOException usually implying a connectivity error reaching the platform server
        @throws ParseException if the platform server's response couldn't be parsed
        @throws UnauthorizedException if, after 2 tries, the client could not authenticate with the
            given client ID and client secret
        @return a {@link Map} of results
    */
    public Map<String, Object> pharmacyNetwork(Map<String, Object> params)
            throws IOException, ParseException, UnauthorizedException {
	    return pharmacyNetwork(null, params);
    }
}
