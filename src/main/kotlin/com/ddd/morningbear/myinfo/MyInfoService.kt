package com.ddd.morningbear.myinfo

import com.ddd.morningbear.api.myinfo.dto.MyInfoInput
import com.ddd.morningbear.badge.BadgeService
import com.ddd.morningbear.category.CategoryService
import com.ddd.morningbear.common.exception.GraphQLBadRequestException
import com.ddd.morningbear.common.exception.GraphQLNotFoundException
import com.ddd.morningbear.myinfo.dto.MpUserInfoDto
import com.ddd.morningbear.myinfo.entity.MpUserInfo
import com.ddd.morningbear.myinfo.repository.MpUserInfoRepository
import com.ddd.morningbear.myinfo.repository.MpUserInfoRepositoryImp
import com.ddd.morningbear.report.ReportService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * @author yoonho
 * @since 2022.12.04
 */
@Service
class MyInfoService(
    private val mpUserInfoRepository: MpUserInfoRepository,
    private val mpUserInfoRepositoryImp: MpUserInfoRepositoryImp,
    private val categoryService: CategoryService,
    private val badgeService: BadgeService,
    private val reportService: ReportService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 내정보 조회
     *
     * @param accountId [String]
     * @return result [MpUserInfoDto]
     * @author yoonho
     * @since 2022.12.04
     */
    fun findUserInfo(accountId: String): MpUserInfoDto {
        var myInfo = mpUserInfoRepository.findById(accountId).orElseThrow {
            throw GraphQLNotFoundException("사용자정보 조회에 실패하였습니다.")
        }.toDto()

        // 뱃지리스트 조회
        myInfo.badgeList = badgeService.findMyAllBadge(accountId)
        // 리포트 조회
        if(myInfo.photoInfo != null){
            myInfo.reportInfo = reportService.createReport(accountId)
        }

        return myInfo
    }

    /**
     * 사용자 검색
     *
     * @param keyword [String]
     * @return List [MpUserInfoDto]
     * @author yoonho
     * @since 2022.12.07
     */
    fun searchUserInfo(keyword: String): List<MpUserInfoDto> = mpUserInfoRepositoryImp.findUserInfoByNickName(keyword).map { it.toDto() }

    /**
     * 내정보 저장
     *
     * @param accountId [String]
     * @param input [MyInfoInput]
     * @return result [MpUserInfoDto]
     * @author yoonho
     * @since 2022.12.04
     */
    @Transactional(rollbackFor = [Exception::class])
    fun saveMyInfo(accountId: String, input: MyInfoInput): MpUserInfoDto {
        if(accountId.isNullOrBlank()){
            throw GraphQLBadRequestException("로그인정보가 존재하지 않습니다.")
        }

        var isRegisteredUser = false
        lateinit var createdAt: LocalDateTime

        try{
            if(mpUserInfoRepository.existsById(accountId)) {
                /* 회원정보 업데이트 */
                val myInfo = mpUserInfoRepository.findById(accountId).orElseThrow { throw GraphQLBadRequestException() }
                // Patch방식으로 이미 저장된 사용자정보에서 input으로 전달받은 데이터만 update
                if(input.nickName.isNullOrBlank()) input.nickName = myInfo.nickName
                if(input.photoLink.isNullOrBlank()) input.photoLink = myInfo.photoLink
                if(input.memo.isNullOrBlank()) input.memo = myInfo.memo
                if(input.wakeUpAt.isNullOrBlank()) input.wakeUpAt = myInfo.wakeUpAt
                createdAt = myInfo.createdAt
            }else{
                isRegisteredUser = true
                createdAt = LocalDateTime.now()
            }

            mpUserInfoRepository.save(
                MpUserInfo(
                    accountId = accountId,
                    nickName = input.nickName,
                    photoLink = input.photoLink,
                    memo = input.memo,
                    wakeUpAt = input.wakeUpAt,
                    updatedAt = LocalDateTime.now(),
                    createdAt = createdAt
                )
            ).toDto()

            // 신규 회원가입시
            if(isRegisteredUser){
                categoryService.saveAllCategory(accountId)
                badgeService.saveMyBadge(accountId, "B1")
            }
            //
            return this.findUserInfo(accountId)
        }catch (ne: GraphQLNotFoundException){
            throw ne
        }catch (be: GraphQLBadRequestException){
            throw be
        }catch(e: Exception){
            throw GraphQLBadRequestException()
        }
    }

    /**
     * 탈퇴하기
     *
     * @param accountId [String]
     * @author yoonho
     * @since 2022.12.04
     */
    @Transactional(rollbackFor = [Exception::class])
    fun deleteMyInfo(accountId: String): Boolean {
        try{
            if(!mpUserInfoRepository.existsById(accountId)) {
                throw GraphQLBadRequestException("이미 탈퇴했거나 존재하지 않는 회원입니다.")
            }

            // 회원테이블 메타정보 삭제
            mpUserInfoRepository.deleteById(accountId)
        }catch(be: GraphQLBadRequestException) {
            throw be
        }catch(e: Exception){
            throw GraphQLBadRequestException()
        }

        return true
    }
}