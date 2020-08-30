/**
 * 
 */
var vm = new Vue({
		el : "#app",
		data : {
			username:"",
			pwd:"",
			result:null,
		},
		methods : {
			subop(){
				if(this.username==null  ||  this.pwd==null){
					this.showresult("请填写完整的信息!");return 
				}
				axios({
					url:"/index/reg",
					method:"post",
					params:{
						name:this.username,
						pwd:this.pwd,
					}
				}).then(function(response){
					if(response.data.code==-1){
						vm.showresult("用户已经存在!")
						return 
					}
					if(response.data.code==-2){
						vm.showresult("非法的用户名!")
						return 
					}
					if(response.data.code==-3){
						vm.showresult("注册失败!")
						return 
					}
					location.href="/index.html"
				})
			},
			showresult(msg){
				this.result=msg
			},
			out(){
				this.result = null
			}
		},
	})