package com.gitee.search.query;

/**
 * 排序工具包，具体使用请参考 QueryHelper
 * @author Winter Lau<javayou@gmail.com>
 * @see com.gitee.search.query.QueryHelper
 */
public class ScoreHelper {

    /**
     * 自定义仓库的搜索评分规则
     * @param score
     * @param recomm
     * @param stars
     * @param gindex
     * @return
     */
    public static double repoSort(double score, double recomm, double stars, double gindex) {
        //TODO 对 score 进行分等级处理
        System.out.printf("score:%.2f, recomm: %f, stars: %f, gindex: %f\n", score, recomm, stars, gindex);
        return score;
    }

    /**
     * 自定义 Issue 评分
     * @param score
     * @return
     */
    public static double issueSort(double score) {
        return 0.1d;
    }

}
