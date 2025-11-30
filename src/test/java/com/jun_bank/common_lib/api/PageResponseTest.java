package com.jun_bank.common_lib.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResponse & PageInfo 테스트")
class PageResponseTest {

    @Nested
    @DisplayName("PageInfo 테스트")
    class PageInfoTest {

        @Test
        @DisplayName("Spring Data Page에서 PageInfo 생성")
        void fromSpringDataPage() {
            // given
            List<String> content = List.of("item1", "item2", "item3");
            PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<String> page = new PageImpl<>(content, pageable, 100);

            // when
            PageInfo pageInfo = PageInfo.from(page);

            // then
            assertThat(pageInfo.getPage()).isEqualTo(0);
            assertThat(pageInfo.getSize()).isEqualTo(10);
            assertThat(pageInfo.getTotalElements()).isEqualTo(100);
            assertThat(pageInfo.getTotalPages()).isEqualTo(10);
            assertThat(pageInfo.getNumberOfElements()).isEqualTo(3);
            assertThat(pageInfo.isFirst()).isTrue();
            assertThat(pageInfo.isLast()).isFalse();
            assertThat(pageInfo.isHasNext()).isTrue();
            assertThat(pageInfo.isHasPrevious()).isFalse();
            assertThat(pageInfo.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("정렬 정보가 올바르게 변환됨")
        void sortInfoConversion() {
            // given
            PageRequest pageable = PageRequest.of(0, 10, Sort.by(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.asc("name")
            ));
            Page<String> page = new PageImpl<>(List.of("item1"), pageable, 1);

            // when
            PageInfo pageInfo = PageInfo.from(page);

            // then
            assertThat(pageInfo.getSort()).hasSize(2);
            assertThat(pageInfo.getSort().get(0).getProperty()).isEqualTo("createdAt");
            assertThat(pageInfo.getSort().get(0).getDirection()).isEqualTo(PageInfo.SortInfo.Direction.DESC);
            assertThat(pageInfo.getSort().get(1).getProperty()).isEqualTo("name");
            assertThat(pageInfo.getSort().get(1).getDirection()).isEqualTo(PageInfo.SortInfo.Direction.ASC);
        }

        @Test
        @DisplayName("정렬 없는 페이지는 sort가 null")
        void noSortInfo() {
            // given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<String> page = new PageImpl<>(List.of("item1"), pageable, 1);

            // when
            PageInfo pageInfo = PageInfo.from(page);

            // then
            assertThat(pageInfo.getSort()).isNull();
        }

        @Test
        @DisplayName("마지막 페이지 여부 확인")
        void lastPage() {
            // given
            List<String> content = List.of("item1");
            PageRequest pageable = PageRequest.of(9, 10);
            Page<String> page = new PageImpl<>(content, pageable, 100);

            // when
            PageInfo pageInfo = PageInfo.from(page);

            // then
            assertThat(pageInfo.isLast()).isTrue();
            assertThat(pageInfo.isHasNext()).isFalse();
            assertThat(pageInfo.isHasPrevious()).isTrue();
        }

        @Test
        @DisplayName("빈 페이지 처리")
        void emptyPage() {
            // given
            Page<String> page = Page.empty();

            // when
            PageInfo pageInfo = PageInfo.from(page);

            // then
            assertThat(pageInfo.isEmpty()).isTrue();
            assertThat(pageInfo.getTotalElements()).isEqualTo(0);
            assertThat(pageInfo.getNumberOfElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("PageResponse 테스트")
    class PageResponseContentTest {

        @Test
        @DisplayName("동일 타입으로 PageResponse 생성")
        void fromSameType() {
            // given
            List<String> content = List.of("item1", "item2", "item3");
            PageRequest pageable = PageRequest.of(0, 10);
            Page<String> page = new PageImpl<>(content, pageable, 100);

            // when
            PageResponse<String> response = PageResponse.from(page);

            // then
            assertThat(response.getContent()).hasSize(3);
            assertThat(response.getContent()).containsExactly("item1", "item2", "item3");
            assertThat(response.getPageInfo()).isNotNull();
            assertThat(response.getPageInfo().getTotalElements()).isEqualTo(100);
        }

        @Test
        @DisplayName("타입 변환으로 PageResponse 생성")
        void fromWithMapper() {
            // given
            record Entity(Long id, String name) {}
            record Dto(String displayName) {}

            List<Entity> content = List.of(
                    new Entity(1L, "홍길동"),
                    new Entity(2L, "김철수")
            );
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Entity> page = new PageImpl<>(content, pageable, 2);

            // when
            PageResponse<Dto> response = PageResponse.from(page,
                    entity -> new Dto("User: " + entity.name()));

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).displayName()).isEqualTo("User: 홍길동");
            assertThat(response.getContent().get(1).displayName()).isEqualTo("User: 김철수");
        }

        @Test
        @DisplayName("직접 생성으로 PageResponse 생성")
        void ofMethod() {
            // given
            List<String> content = List.of("item1", "item2");
            PageInfo pageInfo = PageInfo.builder()
                    .page(0)
                    .size(10)
                    .totalElements(2)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            // when
            PageResponse<String> response = PageResponse.of(content, pageInfo);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getPageInfo().getTotalElements()).isEqualTo(2);
        }
    }
}