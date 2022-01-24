package keeper.project.homepage.service;

import java.util.Optional;
import keeper.project.homepage.entity.CommentEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.MemberHasCommentEntityPK;
import keeper.project.homepage.entity.MemberHasCommentLikeEntity;
import keeper.project.homepage.repository.MemberHasCommentLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberHasCommentLikeService {

  @Autowired
  private MemberHasCommentLikeRepository memberHasCommentLikeRepository;

  public MemberHasCommentLikeEntity save(MemberHasCommentLikeEntity memberHasCommentLikeEntity) {
    return memberHasCommentLikeRepository.save(memberHasCommentLikeEntity);
  }

  public MemberHasCommentLikeEntity saveWithMemberAndCommentEntity(MemberEntity memberEntity,
      CommentEntity commentEntity) {
    return memberHasCommentLikeRepository.save(MemberHasCommentLikeEntity.builder()
        .memberHasCommentEntityPK(new MemberHasCommentEntityPK(memberEntity, commentEntity))
        .build());
  }

  public MemberHasCommentLikeEntity findById(MemberEntity memberEntity,
      CommentEntity commentEntity) {
    Optional<MemberHasCommentLikeEntity> findMHCL = memberHasCommentLikeRepository.findById(
        new MemberHasCommentEntityPK(memberEntity, commentEntity));
    return findMHCL.orElse(null);
  }

  public void deleteByMemberAndCommentEntity(MemberEntity memberEntity,
      CommentEntity commentEntity) {
    memberHasCommentLikeRepository.deleteById(
        new MemberHasCommentEntityPK(memberEntity, commentEntity));
  }
}
