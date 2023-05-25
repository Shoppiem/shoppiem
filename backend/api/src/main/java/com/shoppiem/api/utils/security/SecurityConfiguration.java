package com.shoppiem.api.utils.security;

import com.shoppiem.api.utils.security.firebase.FirebaseSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Configuration
@Import({
        FirebaseSecurityConfiguration.class
})
public class SecurityConfiguration {
}
