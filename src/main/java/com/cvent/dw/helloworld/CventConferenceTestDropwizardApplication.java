package com.cvent.dw.helloworld;

import com.cvent.dw.helloworld.auth.BasicDwAuthenticator;
import com.cvent.dw.helloworld.auth.BasicDwAuthorizer;
import com.cvent.dw.helloworld.cli.RenderCommand;
import com.cvent.dw.helloworld.core.Person;
import com.cvent.dw.helloworld.core.Template;
import com.cvent.dw.helloworld.core.User;
import com.cvent.dw.helloworld.db.PersonDAO;
import com.cvent.dw.helloworld.filter.DateRequiredFeature;
import com.cvent.dw.helloworld.health.TemplateHealthCheck;
import com.cvent.dw.helloworld.resources.FilteredResource;
import com.cvent.dw.helloworld.resources.HelloWorldResource;
import com.cvent.dw.helloworld.resources.PeopleResource;
import com.cvent.dw.helloworld.resources.PersonResource;
import com.cvent.dw.helloworld.resources.ProtectedResource;
import com.cvent.dw.helloworld.resources.ViewResource;
import com.cvent.dw.helloworld.tasks.EchoTask;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.Map;

public class CventConferenceTestDropwizardApplication extends Application<CventConferenceTestDropwizardConfiguration> {
    public static void main(String[] args) throws Exception {
        new CventConferenceTestDropwizardApplication().run(args);
    }

    private final HibernateBundle<CventConferenceTestDropwizardConfiguration> hibernateBundle =
        new HibernateBundle<CventConferenceTestDropwizardConfiguration>(Person.class) {
            @Override
            public DataSourceFactory getDataSourceFactory(CventConferenceTestDropwizardConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<CventConferenceTestDropwizardConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<CventConferenceTestDropwizardConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(CventConferenceTestDropwizardConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<CventConferenceTestDropwizardConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(CventConferenceTestDropwizardConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(CventConferenceTestDropwizardConfiguration configuration, Environment environment) {
        final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.admin().addTask(new EchoTask());
        environment.jersey().register(DateRequiredFeature.class);
        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new BasicDwAuthenticator())
                .setAuthorizer(new BasicDwAuthorizer())
                .setRealm("SUPER SECRET STUFF")
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new PeopleResource(dao));
        environment.jersey().register(new PersonResource(dao));
        environment.jersey().register(new FilteredResource());
    }
}
