package keeper.project.homepage.controller.member;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import keeper.project.homepage.common.FileConversion;
import keeper.project.homepage.dto.result.SingleResult;
import keeper.project.homepage.dto.sign.SignInDto;
import keeper.project.homepage.entity.FileEntity;
import keeper.project.homepage.entity.ThumbnailEntity;
import keeper.project.homepage.entity.member.FriendEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.member.MemberHasMemberJobEntity;
import keeper.project.homepage.entity.member.MemberJobEntity;
import keeper.project.homepage.exception.member.CustomMemberNotFoundException;
import keeper.project.homepage.entity.member.MemberRankEntity;
import keeper.project.homepage.entity.member.MemberTypeEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Log4j2
public class MemberControllerTest extends MemberControllerTestSetup {

  private String userToken;
  private String adminToken;

  final private String loginId = "hyeonmomo";
  final private String password = "keeper";
  final private String realName = "JeongHyeonMo";
  final private String nickName = "JeongHyeonMo";
  final private String emailAddress = "test@k33p3r.com";
  final private String studentId = "201724579";
  final private int point = 100;

  final private String adminLoginId = "hyeonmoAdmin";
  final private String adminPassword = "keeper2";
  final private String adminRealName = "JeongHyeonMo2";
  final private String adminNickName = "JeongHyeonMo2";
  final private String adminEmailAddress = "test2@k33p3r.com";
  final private String adminStudentId = "201724580";
  final private String adminPhoneNumber = "0100100101";
  final private int adminPoint = 50;

  final private String ipAddress1 = "127.0.0.1";

  private MemberEntity memberEntity;
  private ThumbnailEntity thumbnailEntity;
  private FileEntity imageEntity;

  @BeforeAll
  public static void createFile() {
    final String keeperFilesDirectoryPath = System.getProperty("user.dir") + File.separator
        + "keeper_files";
    final String thumbnailDirectoryPath = System.getProperty("user.dir") + File.separator
        + "keeper_files" + File.separator + "thumbnail";
    final String befUpdateImage = keeperFilesDirectoryPath + File.separator + "bef.jpg";
    final String befUpdateThumbnail = thumbnailDirectoryPath + File.separator + "thumb_bef.jpg";
    final String aftUpdateImage = keeperFilesDirectoryPath + File.separator + "aft.jpg";

    File keeperFilesDir = new File(keeperFilesDirectoryPath);
    File thumbnailDir = new File(thumbnailDirectoryPath);

    if (!keeperFilesDir.exists()) {
      keeperFilesDir.mkdir();
    }

    if (!thumbnailDir.exists()) {
      thumbnailDir.mkdir();
    }

    createImageForTest(befUpdateImage);
    createImageForTest(befUpdateThumbnail);
    createImageForTest(aftUpdateImage);
  }

  private static void createImageForTest(String filePath) {
    FileConversion fileConversion = new FileConversion();
    fileConversion.makeSampleJPEGImage(filePath);
  }

  @BeforeEach
  public void setUp() throws Exception {
    imageEntity = FileEntity.builder()
        .fileName("bef.jpg")
        .filePath("keeper_files" + File.separator + "bef.jpg")
        .fileSize(0L)
        .ipAddress(ipAddress1)
        .build();
    fileRepository.save(imageEntity);

    thumbnailEntity = ThumbnailEntity.builder()
        .path("keeper_files" + File.separator + "thumbnail" + File.separator + "thumb_bef.jpg")
        .file(imageEntity).build();
    thumbnailRepository.save(thumbnailEntity);

    MemberJobEntity memberJobEntity = memberJobRepository.findByName("ROLE_회원").get();
    MemberTypeEntity memberTypeEntity = memberTypeRepository.findByName("정회원").get();
    MemberRankEntity memberRankEntity = memberRankRepository.findByName("일반회원").get();
    memberEntity = MemberEntity.builder()
        .loginId(loginId)
        .password(passwordEncoder.encode(password))
        .realName(realName)
        .nickName(nickName)
        .emailAddress(emailAddress)
        .studentId(studentId)
        .point(point)
        .memberType(memberTypeEntity)
        .memberRank(memberRankEntity)
        .thumbnail(thumbnailEntity)
        .generation(getMemberGeneration())
//        .memberJobs(new ArrayList<>(List.of(hasMemberJobEntity)))
        .build();
    memberEntity = memberRepository.save(memberEntity);
    memberTypeEntity.getMembers().add(memberEntity);
    memberRankEntity.getMembers().add(memberEntity);

    MemberHasMemberJobEntity mj = memberHasMemberJobRepository.save(
        MemberHasMemberJobEntity.builder()
            .memberJobEntity(memberJobEntity)
            .memberEntity(memberEntity)
            .build());
    memberJobEntity.getMembers().add(mj);
    memberJobRepository.save(memberJobEntity);
    memberEntity.getMemberJobs().add(mj);
    memberRepository.save(memberEntity);

    String content = "{\n"
        + "    \"loginId\": \"" + loginId + "\",\n"
        + "    \"password\": \"" + password + "\"\n"
        + "}";
    MvcResult result = mockMvc.perform(post("/v1/signin")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andExpect(jsonPath("$.data").exists())
        .andReturn();

    String resultString = result.getResponse().getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    SingleResult<SignInDto> sign = mapper.readValue(resultString, new TypeReference<>() {
    });
    userToken = sign.getData().getToken();

    MemberJobEntity memberAdminJobEntity = memberJobRepository.findByName("ROLE_회장").get();
    MemberHasMemberJobEntity hasMemberAdminJobEntity = MemberHasMemberJobEntity.builder()
        .memberJobEntity(memberAdminJobEntity)
        .build();
    MemberEntity memberAdmin = MemberEntity.builder()
        .loginId(adminLoginId)
        .password(passwordEncoder.encode(adminPassword))
        .realName(adminRealName)
        .nickName(adminNickName)
        .emailAddress(adminEmailAddress)
        .studentId(adminStudentId)
        .point(adminPoint)
        .memberType(memberTypeEntity)
        .memberRank(memberRankEntity)
        .thumbnail(thumbnailEntity)
        .memberJobs(new ArrayList<>(List.of(hasMemberAdminJobEntity)))
        .generation(getMemberGeneration())
        .build();
    memberRepository.save(memberAdmin);

    String adminContent = "{\n"
        + "    \"loginId\": \"" + adminLoginId + "\",\n"
        + "    \"password\": \"" + adminPassword + "\"\n"
        + "}";
    MvcResult adminResult = mockMvc.perform(post("/v1/signin")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(adminContent))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andExpect(jsonPath("$.data").exists())
        .andReturn();

    String adminResultString = adminResult.getResponse().getContentAsString();
    SingleResult<SignInDto> adminSign = mapper.readValue(adminResultString, new TypeReference<>() {
    });
    adminToken = adminSign.getData().getToken();
  }

  @Test
  @DisplayName("잘못된 토큰으로 접근하기")
  public void invalidToken() throws Exception {
    mockMvc.perform(get("/v1/member")
            .header("Authorization", "XXXXXXXXXX"))
        .andDo(print())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(exceptionAdvice.getMessage("accessDenied.code")));
  }

  @Test
  @DisplayName("토큰 없이 접근하기")
  public void noToken() throws Exception {
    mockMvc.perform(get("/v1/member"))
        .andDo(print())
        .andExpect(jsonPath("$.success").value(false));
//        .andExpect(jsonPath("$.code").value(-1002));
  }

  @Test
  @DisplayName("옳은 토큰으로 접근하기")
  public void validToken() throws Exception {
    mockMvc.perform(get("/v1/member")
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("권한이 없을 때")
  public void accessDenied() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get("/v1/admin/members")
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(exceptionAdvice.getMessage("accessDenied.code")));
  }

  @Test
  @DisplayName("권한이 있을 때")
  public void accessAccept() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get("/v1/admin/members")
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.list").exists());
  }

  @Test
  @DisplayName("ADMIN 권한으로 모든 멤버 열람하기")
  public void findAllMember() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get("/v1/admin/members")
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.list").exists());
  }

  @Test
  @DisplayName("기본 권한으로 본인 정보 열람하기")
  public void findMember() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get("/v1/member")
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").exists());
  }

  @Test
  @DisplayName("팔로우하기")
  public void follow() throws Exception {
    String content = "{"
        + "\"followeeLoginId\" : \"" + adminLoginId + "\""
        + "}";
    String docMsg = "팔로우할 회원이 존재하지 않는다면 실패합니다.";
    String docCode = "회원이 존재하지 않을 경우: " + exceptionAdvice.getMessage("memberNotFound.code") + " +\n"
        + "그 외 에러가 발생한 경우: " + exceptionAdvice.getMessage("unKnown.code");
    mockMvc.perform(MockMvcRequestBuilders.post("/v1/member/follow")
            .header("Authorization", userToken)
            .content(content)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("member-follow",
            requestFields(
                fieldWithPath("followeeLoginId").description("팔로우할 회원의 로그인 아이디")
            ),
            responseFields(
                generateCommonResponseFields("성공: true +\n실패: false", docCode, docMsg)
            )
        ));

    MemberEntity memberEntity = memberRepository.findByLoginId(loginId).get();
    MemberEntity memberAdmin = memberRepository.findByLoginId(adminLoginId).get();
    List<FriendEntity> followeeList = memberEntity.getFollowee();
    FriendEntity followee = followeeList.get(followeeList.size() - 1);
    // friend entity에 followee와 follower가 잘 들어갔는지 확인
    assertTrue(followee.getFollowee().equals(memberAdmin));
    assertTrue(followee.getFollower().equals(memberEntity));
  }

  @Test
  @DisplayName("언팔로우하기")
  public void unfollow() throws Exception {
    String content = "{"
        + "\"followeeLoginId\" : \"" + adminLoginId + "\""
        + "}";
    // follow
    MemberEntity memberEntity = memberRepository.findByLoginId(loginId).get();
    MemberEntity memberAdmin = memberRepository.findByLoginId(adminLoginId).get();
    memberService.follow(memberEntity.getId(), adminLoginId);
    List<FriendEntity> followeeList = memberEntity.getFollowee();
    FriendEntity followee = followeeList.get(followeeList.size() - 1);

    // unfollow
    String docMsg = "언팔로우할 회원이 존재하지 않는다면 실패합니다.";
    String docCode = "회원이 존재하지 않을 경우: " + exceptionAdvice.getMessage("memberNotFound.code") + " +\n"
        + "그 외 에러가 발생한 경우: " + exceptionAdvice.getMessage("unKnown.code");
    mockMvc.perform(MockMvcRequestBuilders.delete("/v1/member/unfollow")
            .header("Authorization", userToken)
            .content(content)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("member-unfollow",
            requestFields(
                fieldWithPath("followeeLoginId").description("팔로우한 회원의 로그인 아이디")
            ),
            responseFields(
                generateCommonResponseFields("성공: true +\n실패: false", docCode, docMsg)
            )
        ));

    assertTrue(friendRepository.findById(followee.getId()).isEmpty());
    Assertions.assertFalse(memberEntity.getFollowee().contains(followee));
    Assertions.assertFalse(memberAdmin.getFollower().contains(followee));
  }

  @Test
  @DisplayName("내가 팔로우한 사람 조회하기")
  public void showFollowee() throws Exception {
    // follow: member -> admin(followee)
    MemberEntity memberEntity = memberRepository.findByLoginId(loginId).get();
    memberService.follow(memberEntity.getId(), adminLoginId);

    String docMsg = "";
    String docCode = "에러가 발생한 경우: " + exceptionAdvice.getMessage("unKnown.code");
    mockMvc.perform(get("/v1/member/followee")
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.list[0].nickName").value(adminNickName))
        .andDo(document("member-show-followee",
            responseFields(
                generateMemberCommonResponseFields(ResponseType.LIST,
                    "성공: true +\n실패: false", docCode, docMsg)
            )
        ));
  }


  @Test
  @DisplayName("나를 팔로우한 사람 조회하기")
  public void showFollower() throws Exception {
    // follow: admin(follower) -> member
    MemberEntity memberAdmin = memberRepository.findByLoginId(adminLoginId).get();
    memberService.follow(memberAdmin.getId(), loginId);

    String docMsg = "";
    String docCode = "에러가 발생한 경우: " + exceptionAdvice.getMessage("unKnown.code");
    mockMvc.perform(get("/v1/member/follower")
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.list[0].nickName").value(adminNickName))
        .andDo(document("member-show-follower",
            responseFields(
                generateMemberCommonResponseFields(ResponseType.LIST,
                    "성공: true +\n실패: false", docCode, docMsg)
            )
        ));
  }

  @Test
  @DisplayName("회원 정보 조회(민감한 정보 제외) - 성공")
  public void getAllGeneralMemberInfoSuccess() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get("/v1/members")
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("회원 정보 조회(민감한 정보 제외) - 실패(회원 권한 X)")
  public void getAllGeneralMemberInfoFail() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get("/v1/members")
            .header("Authorization", 1234))
        .andDo(print())
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.msg").value("보유한 권한으로 접근할수 없는 리소스 입니다"));
  }

  @Test
  @DisplayName("회원 탈퇴하기")
  public void deleteAccount() throws Exception {
    String docMsg = "물품을 대여하고 미납한 기록이 남아있을 경우, 또는 입력한 비밀번호가 옳지 않은 경우 탈퇴가 실패합니다.";
    String docCode =
        "물품 미납 기록이 있거나 비밀번호가 틀린 경우: " + exceptionAdvice.getMessage("accountDeleteFailed.code")
            + " +\n" + "그 외 실패한 경우: " + exceptionAdvice.getMessage("unKnown.code");
    mockMvc.perform(delete("/v1/member/delete")
            .param("password", password)
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("member-delete",
            requestParameters(
                parameterWithName("password").description("비밀번호")
            ),
            responseFields(
                generateCommonResponseFields("성공 시: success, 실패 시: fail", docCode, docMsg)
            )
        ));

    Assertions.assertThrows(CustomMemberNotFoundException.class, () -> {
      memberService.findById(memberEntity.getId());
    });
  }
}