package com.marand.meds.app.config;

import com.marand.maf.core.jboss.remoting.DefaultRemoteInvocationFactory;
import com.marand.maf.core.jboss.remoting.RemoteInvocationFactory;
import com.marand.maf.core.openehr.dao.openehr.CoreOpenEhrDao;
import com.marand.maf.core.openehr.dao.openehr.SignificanceOpenEhrDao;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.meds.app.prop.EhrProperties;
import com.marand.thinkehr.session.EhrCommitterProvider;
import com.marand.thinkehr.session.EhrSessionManager;
import com.marand.thinkehr.session.EhrSubjectNamespaceProvider;
import com.marand.thinkehr.session.credentials.EhrSessionCredentialsProvider;
import com.marand.thinkehr.session.credentials.impl.SimpleEhrSessionCredentialsProvider;
import com.marand.thinkehr.session.impl.SimpleCommitterProvider;
import com.marand.thinkehr.session.impl.SimpleEhrSubjectNamespaceProvider;
import com.marand.thinkehr.session.synchronization.EhrSessionSynchronizationManager;
import com.marand.thinkehr.session.synchronization.TransactionalResourceFlusher;
import com.marand.thinkehr.session.synchronization.impl.HibernateSessionFlusher;
import com.marand.thinkehr.session.synchronization.impl.TransactionalEhrSessionSynchronizationManager;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Boris Marn.
 */
@Configuration
@ImportResource({"classpath:com/marand/thinkehr/session/ehr-session.xml", "classpath:/ac-openehr-server-no-advices.xml"})
@EnableConfigurationProperties({EhrProperties.class})
public class MedsEhrConfiguration
{
  @Value("${ehrgate.userId}")
  private String ehrUsername;

  @Value("${ehrgate.password}")
  private String ehrPassword;

  @Value("${ehrgate.subjectNamespace}")
  private String ehrSubjectNamespace;

  @Value("${ehrgate.committer}")
  private String ehrCommitter;

  @Bean
  public EhrSessionCredentialsProvider ehrCredentialsProvider()
  {
    final SimpleEhrSessionCredentialsProvider credentialsProvider = new SimpleEhrSessionCredentialsProvider();
    credentialsProvider.setUsername(ehrUsername);
    credentialsProvider.setPassword(ehrPassword);
    return credentialsProvider;
  }

  @Bean
  public EhrSubjectNamespaceProvider ehrSubjectNamespaceProvider()
  {
    final SimpleEhrSubjectNamespaceProvider namespaceProvider = new SimpleEhrSubjectNamespaceProvider();
    namespaceProvider.setSubjectNamespace(ehrSubjectNamespace);
    return namespaceProvider;
  }

  @Bean
  public EhrCommitterProvider ehrCommitterProvider()
  {
    final SimpleCommitterProvider ehrSubjectNamespaceProvider = new SimpleCommitterProvider();
    ehrSubjectNamespaceProvider.setCommitterName(ehrCommitter);
    return ehrSubjectNamespaceProvider;
  }

  @Bean
  public RemoteInvocationFactory ehrGateRemoteInvocationFactory()
  {
    return new DefaultRemoteInvocationFactory();
  }

  @Bean
  public TransactionalResourceFlusher ehrTransactionalResourceFlusher(SessionFactory hibernateSessionFactory)
  {
    final HibernateSessionFlusher hibernateSessionFlusher = new HibernateSessionFlusher();
    hibernateSessionFlusher.setSessionFactory(hibernateSessionFactory);
    return hibernateSessionFlusher;
  }

  @Bean
  public EhrSessionSynchronizationManager ehrSessionSynchronizationManager(
      final EhrSessionManager ehrSessionManager,
      final TransactionalResourceFlusher transactionalResourceFlusher)
  {
    final TransactionalEhrSessionSynchronizationManager hibernateSessionFlusher = new TransactionalEhrSessionSynchronizationManager();
    hibernateSessionFlusher.setSessionManager(ehrSessionManager);
    hibernateSessionFlusher.setTransactionalResourceFlusher(transactionalResourceFlusher);
    return hibernateSessionFlusher;
  }

  @Bean
  public TaggingOpenEhrDao taggingOpenEhrDao()
  {
    return new TaggingOpenEhrDao();
  }

  @Bean
  public CoreOpenEhrDao ehrCoreDao()
  {
    return new CoreOpenEhrDao();
  }

  @Bean
  public SignificanceOpenEhrDao ehrSignificanceDao()
  {
    return new SignificanceOpenEhrDao();
  }
}
