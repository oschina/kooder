package com.gitee.search.query;

/**
 * 排序工具包，具体使用请参考 QueryHelper
 * @author Winter Lau<javayou@gmail.com>
 * @see com.gitee.search.query.QueryHelper
 */
public class ScoreHelper {

    public final static int SCORE_FACTOR = 6;

    /**
     * 自定义仓库的搜索评分规则
     * @param score
     * @param recomm
     * @param stars
     * @param gindex
     * @return
     */
    public static double repoSort(double score, double recomm, double stars, double gindex) {
        if(score >= SCORE_FACTOR) {
            //官方推荐加权
            if(recomm > 0)
                score += (recomm * 20);
            else {
                //Star 数加权
                if (stars >= 100)
                    score += stars / 20;
                else if (stars > 0 && stars < 10)
                    score -= 10;
                else
                    score -= 20;
            }
            while(score < SCORE_FACTOR)
                score += 1;
        }
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
