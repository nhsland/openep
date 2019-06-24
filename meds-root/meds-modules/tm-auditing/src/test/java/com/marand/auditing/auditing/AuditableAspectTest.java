package com.marand.auditing.auditing;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import care.better.auditing.AuditDto;
import com.google.common.collect.Maps;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.cloud.sleuth.Span;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
public class AuditableAspectTest
{
  @InjectMocks
  private final AuditableAspect auditableAspect = new AuditableAspect(
      null,
      null,
      "OPENeP",
      true,
      null);

  @Before
  public void setUp()
  {
    RequestUser.init(auth -> new UserDto("TestUserID", "TestUsername", "TestFullName", Collections.emptyList()));
  }

  @Test
  public void buildAuditDtoWithSessionIdAndEhrIdTest() throws NoSuchMethodException
  {
    final MethodSignature methodSignature = mock(MethodSignature.class);
    Mockito.when(methodSignature.getMethod()).thenReturn(testMethod());

    final Map<String, String> methodArguments = Maps.newHashMap();
    methodArguments.put("patientId", "1234");
    methodArguments.put("sessionId", "445");
    methodArguments.put("ehrId", "ehr12");

    final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    httpServletRequest.setRemoteAddr("192.168.34.56");

    final AuditDto result = auditableAspect.buildAuditDto(
        Span.builder().spanId(111L).build(),
        methodSignature,
        methodArguments,
        "result",
        null,
        httpServletRequest);

    Assert.assertEquals(methodArguments, result.getArguments());
    Assert.assertNull(result.getErrorMessage());
    Assert.assertTrue(result.getDuration() > 0);
    Assert.assertEquals("testMethod", result.getMethodName());
    Assert.assertEquals("1234", result.getPatientId());
    Assert.assertEquals("111", result.getRequestId());
    Assert.assertEquals("result", result.getResponse());
    Assert.assertEquals("TestUserID", result.getUserId());
    Assert.assertEquals("TestUsername", result.getUsername());
    Assert.assertEquals("TestFullName", result.getUserFullName());
    Assert.assertNull(result.getAdditionalInfo());
    Assert.assertEquals("OPENeP", result.getApplication());
    Assert.assertEquals("ehr12", result.getEhrId());
    Assert.assertEquals("192.168.34.56", result.getOriginIp());
  }

  public Method testMethod() throws NoSuchMethodException
  {
    return getClass().getDeclaredMethod("testMethod");
  }
}
