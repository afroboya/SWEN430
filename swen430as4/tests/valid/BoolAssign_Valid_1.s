
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $1, %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label104
	movq $1, %rax
	jmp label105
label104:
	movq $0, %rax
label105:
	movq %rax, %rdi
	call assertion
	movq $0, %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label106
	movq $1, %rax
	jmp label107
label106:
	movq $0, %rax
label107:
	movq %rax, %rdi
	call assertion
label103:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
