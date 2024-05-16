package com.github.binarywang.demo.wx.mp.controller;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.*;
import com.github.binarywang.wxpay.bean.result.*;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/wx/order")
@AllArgsConstructor
public class WxPayController {
  private WxPayService wxService;

    /**
     * 返回前台H5调用JS支付所需要的参数，公众号支付调用此接口
     */
    @RequestMapping(value = "getJSSDKPayInfo")
    @ResponseBody
    public Map<String, String> getJSSDKPayInfo(@RequestParam String openId) throws WxPayException {
        WxPayUnifiedOrderRequest prepayInfo = WxPayUnifiedOrderRequest.newBuilder()
                .openid(openId)
                .outTradeNo(UUID.randomUUID().toString().substring(0,30))
                .totalFee(1)
                .body("算命")
                .tradeType("JSAPI")
                .spbillCreateIp("43.242.96.20")
                .notifyUrl("https://test.haibucuo.com.cn/pay/wx/order/notify/order")
                .build();
       return this.wxService.getPayInfo(prepayInfo);
    }


  /**
   * <pre>
   * 查询订单(详见https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_2)
   * 该接口提供所有微信支付订单的查询，商户可以通过查询订单接口主动查询订单状态，完成下一步的业务逻辑。
   * 需要调用查询接口的情况：
   * ◆ 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；
   * ◆ 调用支付接口后，返回系统错误或未知交易状态情况；
   * ◆ 调用被扫支付API，返回USERPAYING的状态；
   * ◆ 调用关单或撤销接口API之前，需确认支付状态；
   * 接口地址：https://api.mch.weixin.qq.com/pay/orderquery
   * </pre>
   *
   * @param transactionId 微信订单号
   * @param outTradeNo    商户系统内部的订单号，当没提供transactionId时需要传这个。
   */
  @GetMapping("/queryOrder")
  public WxPayOrderQueryResult queryOrder(@RequestParam(required = false) String transactionId,
                                          @RequestParam(required = false) String outTradeNo)
    throws WxPayException {
    return this.wxService.queryOrder(transactionId, outTradeNo);
  }

  @PostMapping("/queryOrder")
  public WxPayOrderQueryResult queryOrder(@RequestBody WxPayOrderQueryRequest wxPayOrderQueryRequest) throws WxPayException {
    return this.wxService.queryOrder(wxPayOrderQueryRequest);
  }

  /**
   * 调用统一下单接口，并组装生成支付所需参数对象.
   *
   * @param request 统一下单请求参数
   * @param <T>     请使用{@link com.github.binarywang.wxpay.bean.order}包下的类
   * @return 返回 {@link com.github.binarywang.wxpay.bean.order}包下的类对象
   */
  @PostMapping("/createOrder")
  public <T> T createOrder(@RequestBody WxPayUnifiedOrderRequest request) throws WxPayException {
    return this.wxService.createOrder(request);
  }

  /**
   * 统一下单(详见https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_1)
   * 在发起微信支付前，需要调用统一下单接口，获取"预支付交易会话标识"
   * 接口地址：https://api.mch.weixin.qq.com/pay/unifiedorder
   *
   * @param request 请求对象，注意一些参数如appid、mchid等不用设置，方法内会自动从配置对象中获取到（前提是对应配置中已经设置）
   */
  @PostMapping("/unifiedOrder")
  public WxPayUnifiedOrderResult unifiedOrder(@RequestBody WxPayUnifiedOrderRequest request) throws WxPayException {
    return this.wxService.unifiedOrder(request);
  }

  @PostMapping("/notify/order")
  public String parseOrderNotifyResult(@RequestBody String xmlData) throws WxPayException {
    final WxPayOrderNotifyResult notifyResult = this.wxService.parseOrderNotifyResult(xmlData);
    // TODO 根据自己业务场景需要构造返回对象
    return WxPayNotifyResponse.success("成功");
  }
}

