package com.codecraft.documentationgenerator;

import com.codecraft.documentationgenerator.exception.BusinessExceptionTest;
import com.codecraft.documentationgenerator.exception.GlobalExceptionHandlerTest;
import com.codecraft.documentationgenerator.service.impl.ApiKeyServiceImplTest;
import com.codecraft.documentationgenerator.service.impl.DocServiceImplTest;
import com.codecraft.documentationgenerator.service.impl.ExceptionHandlingTest;
import com.codecraft.documentationgenerator.service.impl.TeamServiceImplTest;
import com.codecraft.documentationgenerator.service.impl.UserServiceImplTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    UserServiceImplTest.class,
    DocServiceImplTest.class,
    TeamServiceImplTest.class,
    ApiKeyServiceImplTest.class,
    BusinessExceptionTest.class,
    GlobalExceptionHandlerTest.class,
    ExceptionHandlingTest.class
    // 注意：集成测试不包含在套件中，因为它们需要完整的Spring上下文
})
public class AllTestsSuite {
}