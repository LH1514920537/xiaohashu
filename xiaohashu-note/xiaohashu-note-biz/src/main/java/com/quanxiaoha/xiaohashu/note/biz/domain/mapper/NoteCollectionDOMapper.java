package com.quanxiaoha.xiaohashu.note.biz.domain.mapper;

import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteCollectionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteCollectionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCollectionDO record);

    int insertSelective(NoteCollectionDO record);

    NoteCollectionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCollectionDO record);

    int updateByPrimaryKey(NoteCollectionDO record);

    /**
     * 查询笔记是否被收藏
     * @param userId
     * @param noteId
     * @return
     */
    Integer selectCountByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询用户已收藏的笔记
     * @param userId
     * @return
     */
    List<NoteCollectionDO> selectByUserId(Long userId);

    /**
     * 查询用户最近收藏的笔记
     * @param userId
     * @param limit
     * @return
     */
    List<NoteCollectionDO> selectCollectedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit")  int limit);

    int insertOrUpdate(NoteCollectionDO noteCollectionDO);

    int update2UnCollectByUserIdAndNoteId(NoteCollectionDO noteCollectionDO);
}