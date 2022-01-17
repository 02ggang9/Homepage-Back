package keeper.project.homepage.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import keeper.project.homepage.repository.PostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class PostingControllerTest {

  @Autowired
  private PostingRepository postingRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext ctx;

  @BeforeEach
  public void setUp() throws Exception {
    // mockMvc의 한글 사용을 위한 코드
    this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
        .apply(springSecurity())
        .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
        .alwaysDo(print())
        .build();
  }

  @Test
  public void findAllPosting() throws Exception {

    ResultActions result = mockMvc.perform(
        get("/v1/post/latest")                 // (2)
            .param("page", "1")
            .param("size", "3")
            .contentType(MediaType.APPLICATION_JSON));

    result.andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print());
  }

  @Test
  public void findAllPostingByCategoryId() throws Exception {

    ResultActions result = mockMvc.perform(get("/v1/post/lists")
        .param("page", "0")
        .param("size", "10")
        .param("category", "6")
        .contentType(MediaType.APPLICATION_JSON));

    result.andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print());
  }

  @Test
  public void getPosting() throws Exception {
    Integer postingId = 44;
    ResultActions result = mockMvc.perform(get("/v1/post/" + postingId.toString())
        .contentType(MediaType.APPLICATION_JSON));

    result.andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print());
  }

  @Test
  public void createPosting() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", "mvc제목");
    params.add("content", "mvc내용");
    params.add("memberId", "3");
    params.add("categoryId", "7");
    params.add("visitCount", "1");
    params.add("likeCount", "1");
    params.add("dislikeCount", "1");
    params.add("commentCount", "1");
    params.add("registerTime", "2022-01-12 17:07:19");
    params.add("updateTime", "2022-01-12 17:07:19");
    params.add("ipAddress", "192.111.222");
    params.add("allowComment", "0");
    params.add("isNotice", "0");
    params.add("isSecret", "1");
    params.add("password", "asd");

    ResultActions result = mockMvc.perform(
        multipart("/v1/post/new").contentType(MediaType.MULTIPART_FORM_DATA)
            .params(params)
            .with(request -> {
              request.setMethod("POST");
              return request;
            }));

    result.andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print())
        .andDo(document("post/createPosting",
            requestHeaders(
                headerWithName(HttpHeaders.CONTENT_TYPE).description(
                    MediaType.APPLICATION_JSON_VALUE),
                headerWithName(HttpHeaders.ACCEPT).description(MediaType.MULTIPART_FORM_DATA)
                ),
            requestParameters(
                parameterWithName("title").description("제목"),
                parameterWithName("content").description("내용"),
                parameterWithName("categoryId").description("게시판 종류 ID"),
                parameterWithName("ipAddress").description("IP 주소"),
                parameterWithName("allowComment").description("댓글 허용?"),
                parameterWithName("isNotice").description("공지글?"),
                parameterWithName("isSecret").description("비밀글?"),
                parameterWithName("password").description("비밀번호")
            ),
            requestParts(
                partWithName("files").description("첨부 이미지")
            )
        ));
  }

  //test code 추후 작성
}
