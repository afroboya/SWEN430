
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq 24(%rbp), %rax
	movq $0, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rbx
	movq %rbx, -8(%rbp)
	movq 24(%rbp), %rax
	movq $0, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rbx
	movq %rbx, -16(%rbp)
	movq -16(%rbp), %rax
	movq -8(%rbp), %rbx
	addq %rbx, %rax
	movq %rax, 16(%rbp)
	jmp label426
label426:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	subq $16, %rsp
	movq -8(%rbp), %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $2, %rbx
	cmpq %rax, %rbx
	jnz label428
	movq $1, %rax
	jmp label429
label428:
	movq $0, %rax
label429:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	jmp label430
	movq $1, %rax
	jmp label431
label430:
	movq $0, %rax
label431:
	movq %rax, %rdi
	call assertion
label427:
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
