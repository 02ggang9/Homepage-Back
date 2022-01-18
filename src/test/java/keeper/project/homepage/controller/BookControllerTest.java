package keeper.project.homepage.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import javax.transaction.Transactional;
import keeper.project.homepage.entity.BookEntity;
import keeper.project.homepage.entity.MemberEntity;
import keeper.project.homepage.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@AutoConfigureMockMvc
@Transactional
public class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private WebApplicationContext ctx;

  final private String bookTitle = "Do it! 점프 투 파이썬";
  final private String bookAuthor = "박응용";
  final private String bookPicture = "JumpToPython.png";
  final private String bookInformation = "파이썬의 기본이 잘 정리된 책이다.";
  final private Long bookQuantity = 2L;
  final private Long bookBorrow = 0L;
  final private Long bookEnable = bookQuantity;
  final private String bookRegisterDate = "20220116";

  final private long epochTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();

  @BeforeEach
  public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
    // mockMvc의 한글 사용을 위한 코드
    this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
        .apply(documentationConfiguration(restDocumentation)
            .operationPreprocessors()
            .withRequestDefaults(modifyUris().host("test.com").removePort(), prettyPrint())
            .withResponseDefaults(prettyPrint())
        )
        .build();

    SimpleDateFormat stringToDate = new SimpleDateFormat("yyyymmdd");
    Date registerDate = stringToDate.parse(bookRegisterDate);

    bookRepository.save(
        BookEntity.builder()
            .title(bookTitle)
            .author(bookAuthor)
            .picture(bookPicture)
            .information(bookInformation)
            .total(bookQuantity)
            .borrow(bookBorrow)
            .enable(bookEnable)
            .registerDate(registerDate)
            .build());
  }

  @Test
  @DisplayName("책 등록 성공(기존 책)")
  public void addBook() throws Exception {
    Long bookQuantity1 = 2L;
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle);
    params.add("author", bookAuthor);
    params.add("picture", bookPicture);
    params.add("information", bookInformation);
    params.add("quantity", String.valueOf(bookQuantity1));

    mockMvc.perform(post("/v1/addbook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andDo(document("add-book",
            requestParameters(
                parameterWithName("title").description("책 제목"),
                parameterWithName("author").description("저자"),
                parameterWithName("picture").description("책 표지 사진(없어도 됨)"),
                parameterWithName("information").description("한줄평(없어도 됨)"),
                parameterWithName("quantity").description("추가 할 수량")
            ),
            responseFields(
                fieldWithPath("success").description("책 추가 완료 시 true, 실패 시 false 값을 보냅니다."),
                fieldWithPath("code").description("책 추가 완료 시 0, 수량 초과로 실패 시 -1 코드를 보냅니다."),
                fieldWithPath("msg").description("책 추가 실패가 수량 초과 일 때만 발생하므로 수량 초과 메시지를 발생시킵니다.")
            )));
  }

  @Test
  @DisplayName("수량 초과 책 등록 실패(기존 책)")
  public void addBookFailedOverMax() throws Exception {
    Long bookQuantity1 = 3L;
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle);
    params.add("author", bookAuthor);
    params.add("quantity", String.valueOf(bookQuantity1));

    mockMvc.perform(post("/v1/addbook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("새 책 등록 성공")
  public void addNewBook() throws Exception {
    Long bookQuantity2 = 4L;
    String newTitle = "일반물리학";
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", newTitle);
    params.add("author", bookAuthor);
    params.add("quantity", String.valueOf(bookQuantity2));

    mockMvc.perform(post("/v1/addbook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("수량 초과 새 책 등록 실패")
  public void addNewBookFailedOverMax() throws Exception {
    Long bookQuantity3 = 5L;
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle+epochTime);
    params.add("author", bookAuthor);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/addbook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 삭제 성공(일부 삭제)")
  public void deleteBook() throws Exception {
    Long bookQuantity1 = 1L;
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle);
    params.add("quantity", String.valueOf(bookQuantity1));

    mockMvc.perform(post("/v1/deletebook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andDo(document("delete-book",
            requestParameters(
                parameterWithName("title").description("책 제목"),
                parameterWithName("quantity").description("삭제 할 수량")
            ),
            responseFields(
                fieldWithPath("success").description("책 삭제 완료 시 true, 실패 시 false 값을 보냅니다."),
                fieldWithPath("code").description("책 삭제 완료 시 0, 최대 수량 초과로 실패 시 -1, 없는 책으로 실패 시 -2 코드를 보냅니다."),
                fieldWithPath("msg").description("책 삭제 실패가 수량 초과 일 때 수량 초과 메시지를, 없는 책일 때 책이 없다는 메시지를 발생시킵니다.")
            )));
  }

  @Test
  @DisplayName("책 삭제 성공(전체 삭제)")
  public void deleteBookMax() throws Exception {
    Long bookQuantity3 = 2L;
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/deletebook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 삭제 실패(없는 책)")
  public void deleteBookFailedNoExist() throws Exception {
    Long bookQuantity3 = 1L;
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle+epochTime);
    params.add("author", bookAuthor);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/deletebook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-2))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 삭제 실패(기존보다 많은 수량)")
  public void deleteBookFailedOverMax() throws Exception {
    Long bookQuantity3 = 5L;
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/deletebook").params(params))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }
}
