// src/components/index.js
import NavBar from './common/NavBar.vue';
import EndBar from './common/EndBar.vue';
import CustomButton from './common/CustomButton.vue';
import FriendItem from './business/FriendItem.vue';

// 导出所有组件（支持局部导入）
export {
  NavBar,
  EndBar,
  CustomButton,
  FriendItem,
};

// 全局注册函数（支持在main.js中批量注册）
export const install = (app) => {
  const components = [
    NavBar,
    EndBar,
    CustomButton,
    FriendItem,
  ];

  components.forEach(component => {
    app.component(component.name || component.__name, component);
  });
};

// 默认导出（支持Vue.use()全局注册）
export default { install };