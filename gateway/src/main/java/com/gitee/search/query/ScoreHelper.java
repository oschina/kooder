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
        //System.out.printf("%s -> score:%.2f, recomm: %f, stars: %f, gindex: %f\n", Thread.currentThread().getName(), score, recomm, stars, gindex);
        if(score > 10) {
            //官方推荐加权
            if(recomm > 0)
                score += (recomm * 20);
            else
                score -= 10;
            //Star 数加权
            if(stars >= 100)
                score += (stars/((recomm>0)?10:100));
            else if(stars < 10)
                score -= 10;

            //Gitee Index 加权
            if(gindex > 80)
                score += 15;
            else if(gindex > 50)
                score += 10;

            if(score < 0)
                score = 1;
        }
        //System.out.println(" new score: " + score);
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
