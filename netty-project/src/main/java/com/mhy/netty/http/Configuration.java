package com.mhy.netty.http;


import com.google.common.collect.Lists;
import com.mhy.netty.annotation.RequestMapping;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: mahaiyang
 * Date: 16-1-14
 * Time: 下午5:32
 */
public class Configuration extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);


    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        Controller controller = configuration.build(new DefaultProxyFactory(), Lists.newArrayList("com.mhy.netty"));
        System.out.println(123123);
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
//        configuration.build(new SpringProxyFactory(applicationContext), Lists.newArrayList("com.sohu.smc")).start().get();

//    }

    }
//

    //
    public final Controller build(ProxyFactory factory, List<String> packages) {
      //  final String sysPackageName= MonitorAction.class.getPackage().getName();
      //  packages.add(sysPackageName);
        Set<Class<?>> classSet = PackageScanner.getClasses(RequestMapping.class, packages);  //得到目标包下所有的action类
        Controller controller = new Controller();
        int count = 0;
        Set<String> path = new HashSet<String>();
        try {
            for (Class cls : classSet) {
                RequestMapping t = (RequestMapping) cls.getAnnotation(RequestMapping.class);
                String folderurl = formatClassUrl(t.value()[0]);  //formtter
                Object obj=null;
//
//                if(sysPackageName.equals(cls.getPackage().getName())){//系统Action，调用默认构造方法，直接生成实例
//                    obj=cls.newInstance();
//                }else {
//
//                }
                obj = factory.getObject(cls);    //生成被代理的对象实例
                //开始处理每个方法
                for (Method m : cls.getDeclaredMethods()) { //获得当前类的所有方法，但不包括继承过来的方法
                    if (m.isAnnotationPresent(RequestMapping.class)) {
                        t = m.getAnnotation(RequestMapping.class);

                        String url = folderurl + formatMethodUrl(t.value(), m);   //生成最终的url

                        Class[] ccc = m.getParameterTypes();

//                        if(!(m.getReturnType().getName().equals("void"))&&!(m.getReturnType()==String.class)){
//                            throw new IllegalArgumentException(cls.getName() + "." + m.getName() + " must return String .");
//                        }

                        if (m.getModifiers() != Modifier.PUBLIC) {
                            throw new IllegalArgumentException(cls.getName() + "." + m.getName() + " is not public.");
                        }


                        for (Class c : ccc) {
                            if (c != HttpRequestKit.class && c != HttpResponseKit.class) {    // 如果参数的类型不是request resp的话，也认为是错误的
                               log.error("paramerter must be HttpRequestKit  or HttpResponse");
                                throw new IllegalArgumentException(cls.getName() + "." + m.getName() + " param type error. paramtType="+c);
                            }
                        }


                        //目标类的全路径，只要不重复即可，此处是用被代理类的包 +proxy +method+className 的方式
                        String proxyName = cls.getPackage().getName() + ".proxy." + cls.getSimpleName() + "_" + m.getName() + "_" + (++count);

                        byte[] code = BytesBuilder.dump(proxyName, m);      // get byte code

                        Class<?> exampleClass = this.defineClass(proxyName, code, 0,
                                code.length);                  //load into jvm


                        Constructor c1 = exampleClass.getDeclaredConstructor(new Class[]{cls});
                        Action o = (Action) c1.newInstance(new Object[]{obj});    //new
                        o.setName(url);
//                        System.out.println(o.getName());
//                        o.action(null,null) ;
                        if (path.contains(url)) {
                            throw new IllegalArgumentException(cls.getName() + "." + m.getName() + " url is repeat. url="+url);
                        } else {
                            path.add(url);
                          log.info("found:   "+url);
                            controller.addAction(url, o);
                        }


                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return controller;
    }

    //
//    /**
//     * 格式化url，成为    /folder  样式的
//     *
//     * @param url
//     * @return
//     */
    private String formatClassUrl(String url) {
        url = StringUtils.trimToEmpty(url);
        if (StringUtils.isBlank(url)) {
            return url;
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    //
//
    private String formatMethodUrl(String[] urlArray, Method method) {
        if (urlArray == null || urlArray.length < 1) {
            return "/" + method.getName();
        }
        String url = StringUtils.trimToEmpty(urlArray[0]);
        if (StringUtils.isBlank(url)) {
            return "/" + method.getName().toLowerCase();
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        return url;
    }



}
