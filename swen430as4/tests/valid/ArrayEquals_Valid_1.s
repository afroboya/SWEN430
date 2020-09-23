
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 32(%rbp), %rax
	movq 24(%rbp), %rbx
	jmp label472
	movq $48, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $5, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $69, %rbx
	movq %rbx, 8(%rax)
	movq $81, %rbx
	movq %rbx, 16(%rax)
	movq $85, %rbx
	movq %rbx, 24(%rax)
	movq $65, %rbx
	movq %rbx, 32(%rax)
	movq $76, %rbx
	movq %rbx, 40(%rax)
	jmp label470
	jmp label471
label472:
	movq $80, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $9, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $78, %rbx
	movq %rbx, 8(%rax)
	movq $79, %rbx
	movq %rbx, 16(%rax)
	movq $84, %rbx
	movq %rbx, 24(%rax)
	movq $32, %rbx
	movq %rbx, 32(%rax)
	movq $69, %rbx
	movq %rbx, 40(%rax)
	movq $81, %rbx
	movq %rbx, 48(%rax)
	movq $85, %rbx
	movq %rbx, 56(%rax)
	movq $65, %rbx
	movq %rbx, 64(%rax)
	movq $76, %rbx
	movq %rbx, 72(%rax)
	jmp label470
label471:
label470:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_g:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq 24(%rbp), %rax
	movq %rax, 8(%rsp)
	movq 32(%rbp), %rax
	movq %rax, 16(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, 16(%rbp)
	jmp label473
label473:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	call wl_g
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $48, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $5, %rcx
	movq %rcx, 0(%rbx)
	movq $69, %rcx
	movq %rcx, 8(%rbx)
	movq $81, %rcx
	movq %rcx, 16(%rbx)
	movq $85, %rcx
	movq %rcx, 24(%rbx)
	movq $65, %rcx
	movq %rcx, 32(%rbx)
	movq $76, %rcx
	movq %rcx, 40(%rbx)
	jmp label475
	movq $1, %rax
	jmp label476
label475:
	movq $0, %rax
label476:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	call wl_g
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $80, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $9, %rcx
	movq %rcx, 0(%rbx)
	movq $78, %rcx
	movq %rcx, 8(%rbx)
	movq $79, %rcx
	movq %rcx, 16(%rbx)
	movq $84, %rcx
	movq %rcx, 24(%rbx)
	movq $32, %rcx
	movq %rcx, 32(%rbx)
	movq $69, %rcx
	movq %rcx, 40(%rbx)
	movq $81, %rcx
	movq %rcx, 48(%rbx)
	movq $85, %rcx
	movq %rcx, 56(%rbx)
	movq $65, %rcx
	movq %rcx, 64(%rbx)
	movq $76, %rcx
	movq %rcx, 72(%rbx)
	jmp label477
	movq $1, %rax
	jmp label478
label477:
	movq $0, %rax
label478:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	call wl_g
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $48, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $5, %rcx
	movq %rcx, 0(%rbx)
	movq $69, %rcx
	movq %rcx, 8(%rbx)
	movq $81, %rcx
	movq %rcx, 16(%rbx)
	movq $85, %rcx
	movq %rcx, 24(%rbx)
	movq $65, %rcx
	movq %rcx, 32(%rbx)
	movq $76, %rcx
	movq %rcx, 40(%rbx)
	jmp label479
	movq $1, %rax
	jmp label480
label479:
	movq $0, %rax
label480:
	movq %rax, %rdi
	call assertion
label474:
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
