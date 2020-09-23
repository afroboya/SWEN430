
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $1, %rax
	movq %rax, -8(%rbp)
	movq 32(%rbp), %rax
	movq 24(%rbp), %rbx
	cmpq %rbx, %rax
	jge label136
	movq $0, %rax
	movq %rax, -8(%rbp)
	jmp label136
label136:
	movq -8(%rbp), %rax
	cmpq $0, %rax
	jz label139
	jmp label138
label139:
	movq 32(%rbp), %rax
	movq 24(%rbp), %rbx
	addq %rbx, %rax
	movq %rax, 16(%rbp)
	jmp label135
	jmp label137
label138:
	movq $123, %rax
	movq %rax, 16(%rbp)
	jmp label135
label137:
label135:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq $1, %rax
	movq %rax, 8(%rsp)
	movq $1, %rax
	movq %rax, 16(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $123, %rbx
	cmpq %rax, %rbx
	jnz label141
	movq $1, %rax
	jmp label142
label141:
	movq $0, %rax
label142:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $11, %rax
	movq %rax, 8(%rsp)
	movq $0, %rax
	movq %rax, 16(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $11, %rbx
	cmpq %rax, %rbx
	jnz label143
	movq $1, %rax
	jmp label144
label143:
	movq $0, %rax
label144:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $0, %rax
	movq %rax, 8(%rsp)
	movq $11, %rax
	movq %rax, 16(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $123, %rbx
	cmpq %rax, %rbx
	jnz label145
	movq $1, %rax
	jmp label146
label145:
	movq $0, %rax
label146:
	movq %rax, %rdi
	call assertion
label140:
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
