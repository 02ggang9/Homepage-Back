package keeper.project.homepage.controller.posting;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import keeper.project.homepage.ApiControllerTestHelper;
import keeper.project.homepage.entity.posting.CategoryEntity;
import keeper.project.homepage.entity.posting.CommentEntity;
import keeper.project.homepage.entity.posting.PostingEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Log4j2
public class CommentControllerTest extends ApiControllerTestHelper {

  private PostingEntity postingEntity;
  private CommentEntity commentEntity;
  private CommentEntity replyEntity;
  private MemberEntity userEntity;
  private MemberEntity adminEntity;

  private String userToken;
  private String adminToken;
  
  public List<FieldDescriptor> generateCommonCommentResponse(ResponseType type, String docSuccess,
      String docCode, String docMsg, FieldDescriptor... descriptors) {
    String prefix = type.getReponseFieldPrefix();
    List<FieldDescriptor> commonFields = new ArrayList<>();
    commonFields.addAll(generateCommonResponseFields(docSuccess, docCode, docMsg));
    commonFields.addAll(Arrays.asList(
        fieldWithPath(prefix + ".id").description("댓글 id"),
        fieldWithPath(prefix + ".content").description("댓글 내용"),
        fieldWithPath(prefix + ".registerTime").description("댓글이 처음 등록된 시간"),
        fieldWithPath(prefix + ".updateTime").description("댓글이 수정된 시간"),
        fieldWithPath(prefix + ".ipAddress").description("댓글 작성자의 ip address"),
        fieldWithPath(prefix + ".likeCount").description("좋아요 개수"),
        fieldWithPath(prefix + ".dislikeCount").description("싫어요 개수"),
        fieldWithPath(prefix + ".parentId").description("대댓글인 경우, 부모 댓글의 id"),
        fieldWithPath(prefix + ".writer").optional().description("작성자 (탈퇴한 작성자일 경우 null)"),
        fieldWithPath(prefix + ".writerId").optional().description("작성자 (탈퇴한 작성자일 경우 null)"),
        fieldWithPath(prefix + ".writerThumbnailId").optional().type(Long.TYPE)
            .description("작성자 (탈퇴했을 경우 / 썸네일을 등록하지 않았을 경우 null)")));
    if (descriptors.length > 0) {
      commonFields.addAll(Arrays.asList(descriptors));
    }
    return commonFields;
  }

  @BeforeEach
  public void setUp() throws Exception {
    userEntity = generateMemberEntity(MemberJobName.회원);
    userToken = generateJWTToken(userEntity.getLoginId(), memberPassword);
    adminEntity = generateMemberEntity(MemberJobName.회장);
    adminToken = generateJWTToken(adminEntity.getLoginId(), memberPassword);

    CategoryEntity categoryEntity = generateCategoryEntity();
    postingEntity = generatePostingEntity(userEntity, categoryEntity, 0, 1, 0);
    commentEntity = generateCommentEntity(postingEntity, userEntity, 0L);
    replyEntity = generateCommentEntity(postingEntity, userEntity, commentEntity.getId());
  }

  @Test
  @DisplayName("댓글 생성")
  public void commentCreateTest() throws Exception {
    Long postId = postingEntity.getId();
    String content = "{\n"
        + "    \"content\": \"SDFASFASG\",\n"
        + "    \"ipAddress\": \"672.523.937.636\",\n"
        + "    \"parentId\": \n" + 0L + "\n"
        + "}";

    String docSuccess = "성공: true +\n실패: false";
    String docCode =
        "댓글의 내용이 비어있는 경우: " + exceptionAdvice.getMessage("commentEmptyField.code") + " +\n"
            + "그 외 에러가 난 경우: " + exceptionAdvice.getMessage("unKnown.code");
    String docMsg = "댓글 내용이 비어있는 경우 실패합니다.";
    mockMvc.perform(post("/v1/comment/{postId}", postId)
            .header("Authorization", userToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("comment-create",
            pathParameters(
                parameterWithName("postId").description("댓글을 추가할 게시글의 id")
            ),
            requestFields(
                fieldWithPath("content").description("댓글 내용"),
                fieldWithPath("ipAddress").description("댓글 작성자의 ip address"),
                fieldWithPath("parentId").optional()
                    .description("모댓글: 입력 없음(db에는 0으로 설정), 대댓글: 부모 댓글의 id")
            ),
            responseFields(
                generateCommonResponseFields(docSuccess, docCode, docMsg)
            )));
  }

  @Test
  @DisplayName("댓글 생성 실패 - content가 비어있는 경우")
  public void commentCreateFailTest() throws Exception {
    Long commentParentId = commentEntity.getId();
    Long postId = postingEntity.getId();
    String content = "{\n"
        + "    \"content\": \"\",\n"
        + "    \"ipAddress\": \"672.523.937.636\",\n"
        + "    \"parentId\": \n" + commentParentId + "\n"
        + "}";

    mockMvc.perform(RestDocumentationRequestBuilders.post("/v1/comment/{postId}", postId)
            .header("Authorization", userToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andDo(document("comment-create-fail"));
  }

  @Test
  @DisplayName("댓글 페이징")
  public void showCommentByPostIdTest() throws Exception {
    CommentEntity anotherComment = generateCommentEntity(postingEntity, userEntity, 0L);
    Long commentId = anotherComment.getId();
    for (int i = 0; i < 5; i++) {
      generateCommentEntity(postingEntity, userEntity, commentId);
    }

    commentId = commentEntity.getId();
    for (int i = 0; i < 7; i++) {
      generateCommentEntity(postingEntity, userEntity, commentId);
    }

    String docSuccess = "성공: true +\n실패: false";
    String docCode = "에러가 난 경우: " + exceptionAdvice.getMessage("unKnown.code");
    String docMsg = "페이지 당 댓글의 개수는 최대 10개, 댓글에 달린 대댓글의 개수는 제한 없이 조회됩니다.";
    Long postId = postingEntity.getId();
    mockMvc.perform(
            RestDocumentationRequestBuilders.get("/v1/comment/{postId}", postId)
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", userToken)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("comment-list",
            pathParameters(
                parameterWithName("postId").description("조회할 댓글들이 포함된 게시글의 id")
            ),
            requestParameters(
                parameterWithName("page").description("페이지 번호 (페이지 시작 번호 : 0)"),
                parameterWithName("size").description("한 페이지에 들어갈 댓글 개수 (default : 10)")
            ),
            responseFields(
                generateCommonCommentResponse(ResponseType.LIST, docSuccess, docCode, docMsg,
                    fieldWithPath("list[].checkedLike").description(
                        "좋아요 눌렀는지 확인 (눌렀으면 true, 아니면 false)"),
                    fieldWithPath("list[].checkedDislike").description(
                        "싫어요 눌렀는지 확인 (눌렀으면 true, 아니면 false)"))
            )
        ));
  }

  @Test
  @DisplayName("댓글 삭제 - 성공")
  public void commentDeleteTest() throws Exception {
    Long commentId = replyEntity.getId();
    String docSuccess = "성공: true +\n실패: false";
    String docCode =
        "존재하지 않는 댓글인 경우: " + exceptionAdvice.getMessage("commentNotFound.code") + " +\n"
            + "삭제 중 에러가 난 경우: " + exceptionAdvice.getMessage("unKnown.code");
    String docMsg = "댓글 기록을 완전히 삭제하지 않고 작성자와 댓글 내용, 좋아요와 싫어요 수를 초기화합니다.";
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/v1/comment/{commentId}", commentId)
            .header("Authorization", userToken))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("comment-delete",
            pathParameters(
                parameterWithName("commentId").description("삭제할 댓글의 id")
            ),
            responseFields(
                generateCommonResponseFields(docSuccess, docCode, docMsg)
            )
        ));
  }

  @Test
  @DisplayName("관리자 권한 댓글 삭제 - 성공")
  public void adminCommentDeleteTest() throws Exception {
    String docSuccess = "성공: true +\n실패: false";
    String docCode =
        "존재하지 않는 댓글인 경우: " + exceptionAdvice.getMessage("commentNotFound.code") + " +\n"
            + "삭제 중 에러가 난 경우: " + exceptionAdvice.getMessage("unKnown.code");
    String docMsg = "댓글 기록을 완전히 삭제하지 않고 작성자와 댓글 내용, 좋아요와 싫어요 수를 초기화합니다.";
    Long commentId = replyEntity.getId();
    mockMvc.perform(
            RestDocumentationRequestBuilders.delete("/v1/admin/comment/{commentId}", commentId)
                .header("Authorization", adminToken))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("admin-comment-delete",
            pathParameters(
                parameterWithName("commentId").description("삭제할 댓글의 id")
            ),
            responseFields(
                generateCommonResponseFields(docSuccess, docCode, docMsg)
            )
        ));
  }

  @Test
  @DisplayName("댓글 수정")
  public void commentUpdateTest() throws Exception {
    Long updateId = replyEntity.getId();
    String updateString = "수정한 내용!";
    String updateContent = String.format("{\"content\": \"%s\"}", updateString);

    String docSuccess = "성공: true +\n실패: false";
    String docCode =
        "수정할 내용이 비어있는 경우: " + exceptionAdvice.getMessage("commentEmptyField.code") + " +\n"
            + "존재하지 않는 댓글인 경우: " + exceptionAdvice.getMessage("commentNotFound.code") + " +\n"
            + "그 외 에러가 난 경우: " + exceptionAdvice.getMessage("unKnown.code");
    String docMsg = "수정할 내용이 비어있거나, 수정할 댓글이 존재하지 않는 경우 실패합니다.";
    mockMvc.perform(put("/v1/comment/{commentId}", updateId)
            .header("Authorization", userToken)
            .content(updateContent)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.[?(@.data.content == \"%s\")]", updateString).exists())
        .andDo(document("comment-update",
            pathParameters(
                parameterWithName("commentId").description("수정할 댓글의 id")
            ),
            requestFields(
                fieldWithPath("content").description("수정할 댓글 내용")
            ),
            responseFields(
                generateCommonCommentResponse(ResponseType.SINGLE, docSuccess, docCode, docMsg)
            )));
  }

  @Test
  @DisplayName("댓글 수정 실패 - content가 비어있는 경우")
  public void commentUpdateFailTest() throws Exception {
    Long updateId = replyEntity.getId();
    String updateContent = "{\"content\": \"\"}";
    mockMvc.perform(put("/v1/comment/{commentId}", updateId)
            .header("Authorization", userToken)
            .content(updateContent)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().is4xxClientError())
        .andDo(document("comment-update-fail"));
  }

  @Test
  @DisplayName("댓글 좋아요 추가 및 취소")
  public void updateLikeTest() throws Exception {
    Long commentId = replyEntity.getId();
    Integer befLikeCount = replyEntity.getLikeCount();

    String docSuccess = "성공: true +\n실패: false";
    String docCode =
        "존재하지 않는 댓글인 경우: " + exceptionAdvice.getMessage("commentNotFound.code") + " +\n"
            + "그 외 에러가 난 경우: " + exceptionAdvice.getMessage("unKnown.code");
    String docMsg = "댓글이 존재하지 않는 경우 실패합니다.";
    // 좋아요 추가
    mockMvc.perform(RestDocumentationRequestBuilders.get("/v1/comment/like")
            .header("Authorization", userToken)
            .param("commentId", replyEntity.getId().toString())
            .param("memberId", userEntity.getId().toString()))
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("comment-like",
            requestParameters(
                parameterWithName("commentId").description("좋아요 추가 또는 취소할 댓글의 id"),
                parameterWithName("memberId").description("좋아요 추가 또는 취소를 수행하는 멤버의 id")
            ),
            responseFields(
                generateCommonResponseFields(docSuccess, docCode, docMsg)
            )));
    Integer addLikeCount = commentRepository.findById(commentId).get().getLikeCount();
    Assertions.assertNotNull(memberHasCommentLikeService.findById(userEntity, replyEntity));
    Assertions.assertEquals(befLikeCount + 1, addLikeCount);

    // 좋아요 취소
    mockMvc.perform(RestDocumentationRequestBuilders.get("/v1/comment/like")
            .header("Authorization", userToken)
            .param("commentId", replyEntity.getId().toString())
            .param("memberId", userEntity.getId().toString()))
        .andDo(print())
        .andExpect(status().isOk());
    Integer delLikeCount = commentRepository.findById(commentId).get().getLikeCount();
    Assertions.assertNull(memberHasCommentLikeService.findById(userEntity, replyEntity));
    Assertions.assertEquals(addLikeCount - 1, delLikeCount);
  }

  @Test
  @DisplayName("댓글 싫어요 추가 및 취소")
  public void updateDislikeTest() throws Exception {
    Long commentId = replyEntity.getId();
    Integer befDislikeCount = replyEntity.getDislikeCount();

    String docSuccess = "성공: true +\n실패: false";
    String docCode =
        "존재하지 않는 댓글인 경우: " + exceptionAdvice.getMessage("commentNotFound.code") + " +\n"
            + "그 외 에러가 난 경우: " + exceptionAdvice.getMessage("unKnown.code");
    String docMsg = "댓글이 존재하지 않는 경우 실패합니다.";
    // 싫어요 추가
    mockMvc.perform(RestDocumentationRequestBuilders.get("/v1/comment/dislike")
            .header("Authorization", userToken)
            .param("commentId", replyEntity.getId().toString())
            .param("memberId", userEntity.getId().toString()))
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("comment-dislike",
            requestParameters(
                parameterWithName("commentId").description("싫어요 추가 또는 취소할 댓글의 id"),
                parameterWithName("memberId").description("싫어요 추가 또는 취소를 수행하는 멤버의 id")
            ),
            responseFields(
                generateCommonResponseFields(docSuccess, docCode, docMsg)
            )));
    Integer addDislikeCount = commentRepository.findById(commentId).get().getDislikeCount();
    Assertions.assertNotNull(memberHasCommentDislikeService.findById(userEntity, replyEntity));
    Assertions.assertEquals(befDislikeCount + 1, addDislikeCount);

    // 싫어요 취소
    mockMvc.perform(RestDocumentationRequestBuilders.get("/v1/comment/dislike")
            .header("Authorization", userToken)
            .param("commentId", replyEntity.getId().toString())
            .param("memberId", userEntity.getId().toString()))
        .andDo(print())
        .andExpect(status().isOk());
    Integer delDislikeCount = commentRepository.findById(commentId).get().getDislikeCount();
    Assertions.assertNull(memberHasCommentDislikeService.findById(userEntity, replyEntity));
    Assertions.assertEquals(addDislikeCount - 1, delDislikeCount);
  }
}
