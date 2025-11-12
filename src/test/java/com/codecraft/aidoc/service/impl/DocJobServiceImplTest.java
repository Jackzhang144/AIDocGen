package com.codecraft.aidoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.codecraft.aidoc.enums.JobState;
import com.codecraft.aidoc.mapper.DocJobMapper;
import com.codecraft.aidoc.pojo.dto.DocGenerationResult;
import com.codecraft.aidoc.pojo.entity.DocJobEntity;
import com.codecraft.aidoc.pojo.request.DocGenerationJobRequest;
import com.codecraft.aidoc.service.DocService;
import com.codecraft.aidoc.service.DocumentationService;
import com.codecraft.aidoc.service.TelemetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.ResultHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the asynchronous job pipeline to ensure status transitions occur as expected.
 */
class DocJobServiceImplTest {

    private DocumentationService documentationService;
    private StubDocService docService;
    private TelemetryService telemetryService;
    private ThreadPoolTaskExecutor executor;
    private DocJobServiceImpl docJobService;
    private InMemoryDocJobMapper docJobMapper;

    @BeforeEach
    void setUp() {
        documentationService = request -> DocGenerationResult.builder().documentation("Generated doc").build();
        docService = new StubDocService();
        telemetryService = new StubTelemetryService();
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.afterPropertiesSet();
        docJobMapper = new InMemoryDocJobMapper();
        docJobService = new DocJobServiceImpl(documentationService, docService, telemetryService, docJobMapper, new ObjectMapper(), executor);
        docJobService.configureExecutor();
    }

    @Test
    void completesSelectionJob() throws InterruptedException {
        DocGenerationJobRequest request = new DocGenerationJobRequest();
        request.setCode("function add(a, b) { return a + b; }");
        request.setLanguageId("javascript");
        request.setCommented(true);

        String jobId = docJobService.submitSelectionJob(1L, request);

        for (int i = 0; i < 10; i++) {
            var current = docJobService.getJob(1L, jobId).getState();
            if (current == JobState.SUCCEEDED) {
                break;
            }
            Thread.sleep(100);
        }

        var status = docJobService.getJob(1L, jobId);
        assertEquals(JobState.SUCCEEDED, status.getState());
        assertNotNull(status.getResult());
        assertTrue(docService.recorded.get());
    }

    private static class StubDocService implements DocService {
        private final AtomicBoolean recorded = new AtomicBoolean(false);

        @Override
        public void recordGeneration(DocGenerationJobRequest request, DocGenerationResult result, String feedbackId, long elapsedMs) {
            recorded.set(true);
        }

        @Override
        public void recordFeedback(String feedbackId, int score) {
            // no-op for tests
        }

        @Override
        public void recordMetadata(String feedbackId, String fieldName, String value) {
            // no-op for tests
        }
    }

    private static class StubTelemetryService implements TelemetryService {
        @Override
        public void track(String userId, String eventName, java.util.Map<String, ?> properties) {
            // Intentionally left blank for unit testing
        }
    }

    private static class InMemoryDocJobMapper implements DocJobMapper {
        private final Map<String, DocJobEntity> store = new ConcurrentHashMap<>();

        @Override
        public int insert(DocJobEntity entity) {
            store.put(entity.getJobId(), cloneEntity(entity));
            return 1;
        }

        @Override
        public int deleteById(Serializable id) {
            store.remove(id);
            return 1;
        }

        @Override
        public int deleteById(DocJobEntity entity) {
            return deleteById(entity.getJobId());
        }

        @Override
        public int delete(Wrapper<DocJobEntity> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int deleteBatchIds(Collection<?> idList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int updateById(DocJobEntity entity) {
            store.put(entity.getJobId(), cloneEntity(entity));
            return 1;
        }

        @Override
        public int update(DocJobEntity entity, Wrapper<DocJobEntity> updateWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DocJobEntity selectById(Serializable id) {
            DocJobEntity entity = store.get(id);
            return entity == null ? null : cloneEntity(entity);
        }

        @Override
        public List<DocJobEntity> selectBatchIds(Collection<? extends Serializable> idList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectBatchIds(Collection<? extends Serializable> idList, ResultHandler<DocJobEntity> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long selectCount(Wrapper<DocJobEntity> queryWrapper) {
            return (long) store.size();
        }

        @Override
        public List<DocJobEntity> selectList(Wrapper<DocJobEntity> queryWrapper) {
            return new ArrayList<>(store.values());
        }

        @Override
        public void selectList(Wrapper<DocJobEntity> queryWrapper, ResultHandler<DocJobEntity> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DocJobEntity> selectList(IPage<DocJobEntity> page, Wrapper<DocJobEntity> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectList(IPage<DocJobEntity> page, Wrapper<DocJobEntity> queryWrapper, ResultHandler<DocJobEntity> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Map<String, Object>> selectMaps(Wrapper<DocJobEntity> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectMaps(Wrapper<DocJobEntity> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Map<String, Object>> selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<DocJobEntity> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<DocJobEntity> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> List<E> selectObjs(Wrapper<DocJobEntity> queryWrapper) {
            return List.of();
        }

        @Override
        public <E> void selectObjs(Wrapper<DocJobEntity> queryWrapper, ResultHandler<E> resultHandler) {
            throw new UnsupportedOperationException();
        }

        private DocJobEntity cloneEntity(DocJobEntity source) {
            DocJobEntity copy = new DocJobEntity();
            copy.setJobId(source.getJobId());
            copy.setUserId(source.getUserId());
            copy.setState(source.getState());
            copy.setReason(source.getReason());
            copy.setPayload(source.getPayload());
            copy.setResult(source.getResult());
            copy.setCreatedAt(source.getCreatedAt());
            copy.setUpdatedAt(source.getUpdatedAt());
            return copy;
        }
    }
}
