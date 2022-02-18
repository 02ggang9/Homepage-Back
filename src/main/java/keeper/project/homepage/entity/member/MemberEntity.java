package keeper.project.homepage.entity.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import keeper.project.homepage.entity.ThumbnailEntity;
import keeper.project.homepage.entity.posting.PostingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder                    // builder를 사용할수 있게 합니다.
@Entity                     // jpa entity임을 알립니다.
@Getter                     // user 필드값의 getter를 자동으로 생성합니다.
@NoArgsConstructor          // 인자없는 생성자를 자동으로 생성합니다.
@AllArgsConstructor         // 인자를 모두 갖춘 생성자를 자동으로 생성합니다.
@Table(name = "member")     // 'member' 테이블과 매핑됨을 명시
public class MemberEntity implements UserDetails, Serializable {

  @Id // pk
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "login_id", length = 80, nullable = false, unique = true)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String loginId;

  @Column(name = "email_address", length = 250, nullable = false, unique = true)
  private String emailAddress;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Json 결과로 출력 안 할 데이터
  @Column(name = "password", length = 512, nullable = false)
  private String password;

  @Column(name = "real_name", length = 40, nullable = false)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String realName;

  @Column(name = "nick_name", length = 40, nullable = false)
  private String nickName;

  @Column(name = "birthday")
  private Date birthday;

  @Column(name = "student_id", length = 45, nullable = false, unique = true)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String studentId;

  @CreationTimestamp
  @Column(name = "register_date")
  private Date registerDate;

  @ManyToOne
  @JoinColumn(name = "member_type_id")
  @NotFound(action = NotFoundAction.IGNORE)
  // DEFAULT 1
  private MemberTypeEntity memberType;

  @ManyToOne
  @JoinColumn(name = "member_rank_id")
  @NotFound(action = NotFoundAction.IGNORE)
  // DEFAULT 1
  private MemberRankEntity memberRank;

  @Column(name = "point", nullable = false)
  private Integer point;

  @Column(name = "level", nullable = false)
  private Integer level;

  @Column(name = "merit", nullable = false)
  private Integer merit;

  @Column(name = "demerit", nullable = false)
  private Integer demerit;

  @Column(name = "generation", nullable = false)
  private Float generation;

  @OneToOne
  @JoinColumn(name = "thumbnail_id")
  // DEFAULT 1
  private ThumbnailEntity thumbnail;

  @OneToMany(mappedBy = "follower")
  @Builder.Default
  private List<FriendEntity> follower = new ArrayList<>();

  @OneToMany(mappedBy = "followee")
  @Builder.Default
  private List<FriendEntity> followee = new ArrayList<>();

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  public void changeRealName(String newRealName) {
    this.realName = newRealName;
  }

  public void changeNickName(String newNickName) {
    this.nickName = newNickName;
  }

  public void changeStudentId(String newStudentId) {
    this.studentId = newStudentId;
  }

  public void changeEmailAddress(String newEmailAddress) {
    this.emailAddress = newEmailAddress;
  }

  public void changeThumbnail(ThumbnailEntity newThumbnail) {
    this.thumbnail = newThumbnail;
  }

  public void changeMemberRank(MemberRankEntity memberRankEntity) {
    this.memberRank = memberRankEntity;
  }

  public void changeMemberType(MemberTypeEntity memberTypeEntity) {
    this.memberType = memberTypeEntity;
  }

  @OneToMany(mappedBy = "memberEntity")
  @Builder.Default
  private List<MemberHasMemberJobEntity> memberJobs = new ArrayList<>();

  @OneToMany(targetEntity = PostingEntity.class, mappedBy = "memberId", fetch = FetchType.LAZY)
  @Builder.Default
  private List<PostingEntity> posting = new ArrayList<>();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> roles = new ArrayList<>();

    for (MemberHasMemberJobEntity memberJob : this.getMemberJobs()) {
      roles.add(new SimpleGrantedAuthority(memberJob.getMemberJobEntity().getName()));
    }
    return roles;
  }

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Override
  public String getPassword() {
    return password;
  }

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Override
  public String getUsername() {
    return String.valueOf(this.id);
  }

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Override
  public boolean isEnabled() {
    return true;
  }

  public void updatePoint(int point) {
    this.point = point;
  }

  public void increaseMerit(int point) {
    this.merit += point;
  }

  public void decreaseMerit(int point) {
    this.merit -= point;
  }

  public void increaseDemerit(int point) {
    this.demerit += point;
  }

  public void decreaseDemerit(int point) {
    this.demerit -= point;
  }

  public void changeGeneration(float generation) {
    this.generation = generation;
  }

  @PrePersist
  private void prePersist() {
    this.point = (this.point == null ? 0 : this.point);
    this.level = (this.level == null ? 0 : this.level);
    this.merit = (this.merit == null ? 0 : this.merit);
    this.demerit = (this.demerit == null ? 0 : this.demerit);
  }
}