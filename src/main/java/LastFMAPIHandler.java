import de.umass.lastfm.Artist;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Session;

import java.util.ArrayList;

/**
 * Created by Xyline on 15/05/2016.
 */
public class LastFMAPIHandler {

    private String apiKey;
    private String user;
    private String password;
    private String secret;
    private Session session;

    public LastFMAPIHandler() {
        this.apiKey = "4c9ae349ba011cd1ef2ecc315d4efa13"; //this is the key used in the Last.fm API examples
        this.user = "SynerC";
        this.password = "shaner45"; // user's password
        this.secret = "b108c2e7f02dabde369639accb80c8a8";   // api secret
        this.session = Authenticator.getMobileSession(this.user, this.password, this.apiKey, this.secret);
    }

    public String getApiKey() {
        return apiKey;
    }


}
