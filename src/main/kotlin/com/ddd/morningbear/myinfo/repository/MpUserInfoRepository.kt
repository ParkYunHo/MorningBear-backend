package com.ddd.morningbear.myinfo.repository

import com.ddd.morningbear.myinfo.entity.MpUserInfo
import org.springframework.data.jpa.repository.JpaRepository

/**
 * @author yoonho
 * @since 2022.12.04
 */
interface MpUserInfoRepository: JpaRepository<MpUserInfo, String>, MpUserInfoRepositoryDsl

interface MpUserInfoRepositoryDsl {

}